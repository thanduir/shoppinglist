package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GroceryPlanning implements Parcelable
{
    private static int     SERIALIZING_VERSION = 1;

    Categories      m_Categories;
    Ingredients     m_Ingredients;
    Recipes         m_Recipes;
    ShoppingList    m_ShoppingList;

    GroceryPlanning()
    {
        m_Categories = new Categories();
        m_Ingredients = new Ingredients(m_Categories);
        m_Recipes = new Recipes();
        m_ShoppingList = new ShoppingList();
    }

    GroceryPlanning(File file, Context context)
    {
        loadDataFromFile(file, context);
    }

    void saveDataToFile(File fileToBeCreated, Context context)
    {
        boolean moveFilesFirst = fileToBeCreated.exists();

        // Concept if the file already exists:
        //      * write to filename.new
        //      * move existing to filename.old
        //      * move filename.new to filename
        //      * delete filename.old

        File fileNew;
        if(moveFilesFirst)
        {
            fileNew = new File(fileToBeCreated.getAbsolutePath() + ".new");
        }
        else
        {
            fileNew = fileToBeCreated;
        }

        try
        {
            FileWriter fw = new FileWriter(fileNew, false);
            JsonWriter jw = new JsonWriter(fw);
            jw.setIndent("  ");

            jw.beginArray();

            jw.beginObject();
            jw.name("id").value("ch.phwidmer.einkaufsliste");
            jw.name("version").value(SERIALIZING_VERSION);
            jw.endObject();

            m_Categories.saveToJson(jw);
            m_Ingredients.saveToJson(jw);
            m_Recipes.saveToJson(jw);
            m_ShoppingList.saveToJson(jw);

            jw.endArray();

            jw.close();
            fw.close();
        }
        catch(IOException e)
        {
            Toast.makeText(context, R.string.text_save_file_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        if(moveFilesFirst)
        {
            File fileOld = new File(fileToBeCreated.getAbsolutePath() + ".old");
            if(!fileToBeCreated.renameTo(fileOld))
            {
                fileNew.delete();
                Toast.makeText(context, R.string.text_save_file_failed, Toast.LENGTH_SHORT).show();
            }
            if(!fileNew.renameTo(fileToBeCreated))
            {
                fileOld.renameTo(fileToBeCreated);
                Toast.makeText(context, R.string.text_save_file_failed, Toast.LENGTH_SHORT).show();
            }

            fileOld.delete();
        }

        scanFile(context, fileToBeCreated);
    }

    void loadDataFromFile(File file, Context context)
    {
        try
        {
            FileReader fw = new FileReader(file);
            JsonReader jr = new JsonReader(fw);

            jr.beginArray();

            jr.beginObject();
            boolean bIDFound = false;
            int iVersion = -1;
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("id")) {
                    String id = jr.nextString();
                    if (!id.equals("ch.phwidmer.einkaufsliste"))
                    {
                        throw new IOException();
                    }
                    bIDFound = true;
                }
                else if (name.equals("version")) {
                    iVersion = jr.nextInt();
                    if (iVersion > SERIALIZING_VERSION)
                    {
                        throw new IOException();
                    }
                }
            }
            jr.endObject();
            if(!bIDFound || iVersion == -1)
            {
                throw new IOException();
            }

            m_Categories = new Categories();
            m_Categories.readFromJson(jr);

            m_Ingredients = new Ingredients(m_Categories);
            m_Ingredients.readFromJson(jr);

            m_Recipes = new Recipes();
            m_Recipes.readFromJson(jr);

            m_ShoppingList = new ShoppingList();
            m_ShoppingList.readFromJson(jr);

            jr.endArray();

            jr.close();
            fw.close();
        }
        catch(IOException e)
        {
            Toast.makeText(context, R.string.text_load_file_failed, Toast.LENGTH_SHORT).show();
            m_Categories = new Categories();
            m_Ingredients = new Ingredients(m_Categories);
            m_Recipes = new Recipes();
            m_ShoppingList = new ShoppingList();
        }
    }

    // Make file known to the MediaScanner so that it apears when the device is mount e.g. on windows.
    void scanFile(Context context, File f)
    {
        if(context == null)
        {
            return;
        }

        MediaScannerConnection.scanFile(context,
                new String[] {f.getAbsolutePath()},
                new String[] {"application/json"},
                null);
    }

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        m_Categories.writeToParcel(out, flags);
        m_Ingredients.writeToParcel(out, flags);
        m_Recipes.writeToParcel(out, flags);
        m_ShoppingList.writeToParcel(out, flags);
    }

    private GroceryPlanning(Parcel in)
    {
        m_Categories = Categories.CREATOR.createFromParcel(in);
        m_Ingredients = new Ingredients(in, m_Categories);
        m_Recipes = Recipes.CREATOR.createFromParcel(in);
        m_ShoppingList = ShoppingList.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<GroceryPlanning> CREATOR
            = new Parcelable.Creator<GroceryPlanning>() {

        @Override
        public GroceryPlanning createFromParcel(Parcel in) {
            return new GroceryPlanning(in);
        }

        @Override
        public GroceryPlanning[] newArray(int size) {
            return new GroceryPlanning[size];
        }
    };
}
