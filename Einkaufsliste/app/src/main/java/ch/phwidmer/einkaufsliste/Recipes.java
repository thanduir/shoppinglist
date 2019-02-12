package ch.phwidmer.einkaufsliste;

import java.util.LinkedList;

public class Recipes {

    // TODO Speichert ALLE Recipes. Jedes Recipe hat folgende Infos:
    public class Recipe {
        private Integer m_ID;
        private String m_Name;
        private LinkedList<RecipeItem> m_Items;
        private Integer m_NumberOfPersons;
    }
}
