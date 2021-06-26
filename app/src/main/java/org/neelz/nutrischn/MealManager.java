package org.neelz.nutrischn;

import java.util.ArrayList;

class MealManager {
    class Macros {
        Macros(String name, String f, String c, String p) {
            this.name = name;
            this.fat = f;
            this.carbs = c;
            this.protein = p;
        }

        String name;
        String fat;
        String carbs;
        String protein;
    }

    MealManager() {
        m_macros = new ArrayList<>();
    }

    boolean add(String name, String f, String c, String p) {
        for (Macros mv : m_macros) {
            if (mv.name.equals(name)) {
                if (mv.fat.equals(f) && mv.carbs.equals(c) && mv.protein.equals(p)) {
                    return false;
                }
                mv.fat = f;
                mv.carbs = c;
                mv.protein = p;
                return true;
            }
        }
        m_macros.add(new Macros(name, f, c, p));
        m_namesChanged = true;
        return true;
    }

    boolean remove(String name) {
        for (Macros mv : m_macros) {
            if (mv.name.equals(name)) {
                m_macros.remove(mv);
                m_namesChanged = true;
                return true;
            }
        }
        return false;
    }

    CharSequence[] getMealNames() {
        if (m_namesChanged) {
            m_names = new CharSequence[m_macros.size()];
            for (int i = 0; i < m_macros.size(); ++i) {
                m_names[i] = m_macros.get(i).name;
            }
        }
        m_namesChanged = false;
        return m_names;
    }

    boolean m_namesChanged = false;
    ArrayList<Macros> m_macros;
    CharSequence[] m_names;
}
