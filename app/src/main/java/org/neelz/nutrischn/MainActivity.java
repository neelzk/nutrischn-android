package org.neelz.nutrischn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private final String logTag = "NutrischnTag";

    EditText edActualProtein;
    EditText edActualFat;
    EditText edActualCarbs;
    EditText edActualkCal;

    EditText edName;
    EditText edNewProtein;
    EditText edNewFat;
    EditText edNewCarbs;
    EditText edMenge;
    EditText edKcalNew;

    // macros in grams
    int m_protein, m_fat, m_carbs;

    boolean m_valuesChanged = false;

    private class MealValues {
        String name;
        String fat;
        String carbs;
        String protein;
    }

    ArrayList<MealValues> m_mealValues;

    private CharSequence[] getMealNames() {
        CharSequence[] ret = new CharSequence[m_mealValues.size()];
        for (int i = 0; i < m_mealValues.size(); ++i) {
            ret[i] = m_mealValues.get(i).name;
        }
        return ret;
    }

    String m_json;
    JSONArray m_mealsJson;

    int m_mealChoice = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(docDir, "nutvalues.yaml");
        String json = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            json = new Yaml().load(fis).toString();
        } catch (FileNotFoundException e) {
            json = "[{\"n\":\"Hafer\",\"f\":\"7.0\",\"c\":\"58.7\",\"p\":\"13.5\"},{\"n\":\"Nuss\",\"f\":\"57\",\"c\":\"10\",\"p\":\"16\"},{\"n\":\"Kartoffeln\",\"f\":\"0.1\",\"c\":\"15.5\",\"p\":\"2.0\"},{\"n\":\"Batate\",\"f\":\"0.6\",\"c\":\"22\",\"p\":\"1.7\"},{\"n\":\"Moehre\",\"f\":\"0.2\",\"c\":\"4.8\",\"p\":\"1.0\"},{\"n\":\"Banane\",\"f\":\"0.1\",\"c\":\"14.3\",\"p\":\"0.8\"},{\"n\":\"Oliven\",\"f\":\"16\",\"c\":\"0.5\",\"p\":\"1.0\"},{\"n\":\"Reis\",\"f\":\"1.0\",\"c\":\"77.3\",\"p\":\"7.3\"},{\"n\":\"Chiasamen\",\"f\":\"33.3\",\"c\":\"0.33\",\"p\":\"21.6\"},{\"n\":\"Leinsamen\",\"f\":\"31.0\",\"c\":\"0.0\",\"p\":\"22.0\"},{\"n\":\"Rosinen\",\"f\":\"0.5\",\"c\":\"70\",\"p\":\"2.5\"},{\"n\":\"Eier\",\"f\":\"9.8\",\"c\":\"0.1\",\"p\":\"11.5\"},{\"n\":\"Kakao\",\"f\":\"20\",\"c\":\"8.0\",\"p\":\"20\"},{\"n\":\"Kokos\",\"f\":\"65.2\",\"c\":\"8.4\",\"p\":\"7.4\"},{\"n\":\"Hanfprotein\",\"f\":\"5.9\",\"c\":\"8.5\",\"p\":\"41.9\"},{\"n\":\"Quinoapops\",\"f\":\"5.2\",\"c\":\"68.0\",\"p\":\"13.0\"},{\"n\":\"Whey\",\"f\":\"5.8\",\"c\":\"3.8\",\"p\":\"76.2\"}]";
        }

        try {
            m_mealsJson = new JSONArray(json);
            m_mealValues = new ArrayList<MealValues>();

            for (int i = 0; i < m_mealsJson.length(); ++i) {
                MealValues mv = new MealValues();
                JSONObject mealObj = m_mealsJson.getJSONObject(i);
                try {
                    mv.name = mealObj.getString("n");
                } catch (JSONException e5) {
                    mv.name = "Unkown";
                }
                try {
                    mv.fat = mealObj.getString("f");
                } catch (JSONException e2) {
                    mv.fat = "0.0";
                }
                try {
                    mv.carbs = mealObj.getString("c");
                } catch (JSONException e3) {
                    mv.carbs = "0.0";
                }
                try {
                    mv.protein = mealObj.getString("p");
                } catch (JSONException e4) {
                    mv.protein = "0.0";
                }
                m_mealValues.add(mv);
            }
        } catch (JSONException e) {
        }

        edActualFat = (EditText) findViewById(R.id.edActualFat);
        edActualCarbs = (EditText) findViewById(R.id.edActualCarbs);
        edActualProtein = (EditText) findViewById(R.id.edActualProtein);
        edActualkCal = (EditText) findViewById(R.id.edActualkCal);

        edName = (EditText) findViewById(R.id.edName);
        edNewFat = (EditText) findViewById(R.id.edNewFat);
        edNewCarbs = (EditText) findViewById(R.id.edNewCarbs);
        edNewProtein = (EditText) findViewById(R.id.edNewProtein);
        edMenge = (EditText) findViewById(R.id.edMenge);
        edKcalNew = (EditText) findViewById(R.id.edKcalNew);

        SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);

        m_fat = prefs.getInt("fat", 0);
        m_carbs = prefs.getInt("carbs", 0);
        m_protein = prefs.getInt("protein", 0);

        setActualValues();

        findViewById(R.id.fab_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectMealClicked();
            }
        });

        View.OnClickListener edMacroClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edView = (EditText) view;
                if (edView.getText().toString().equals("0") || view.getId() == R.id.edName) {
                    edView.setText("");
                }
            }
        };

        View.OnFocusChangeListener edMacroFocusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText edView = (EditText) view;
                if (hasFocus) {
                    if (edView.getText().toString().equals("0")) {
                        edView.setText("");
                    }
                } else {
                    if (edView.getText().toString().isEmpty()) {
                        edView.setText("0");
                    }
                }
            }
        };

        edNewFat.setOnClickListener(edMacroClickListener);
        edNewCarbs.setOnClickListener(edMacroClickListener);
        edNewProtein.setOnClickListener(edMacroClickListener);
        edName.setOnClickListener(edMacroClickListener);

        edNewFat.setOnFocusChangeListener(edMacroFocusListener);
        edNewCarbs.setOnFocusChangeListener(edMacroFocusListener);
        edNewProtein.setOnFocusChangeListener(edMacroFocusListener);
        edMenge.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText edView = (EditText) view;
                if (hasFocus) {
                    String mengeStr = edView.getText().toString();
                    if (mengeStr.equals("100") || mengeStr.equals("0")) {
                        edView.setText("");
                    }
                } else {
                    if (edView.getText().toString().isEmpty()) {
                        edView.setText("100");
                    }
                }
            }
        });
        edMenge.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(logTag, "Edit done");
                    calcTempCalories();
                }
                return false;
            }
        });

        findViewById(R.id.fab_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEatClicked();
            }
        });

//        findViewById(R.id.fab_clear).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onClearClicked();
//            }
//        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (m_valuesChanged) {
            saveValues();
        }
    }

    private void calcTempCalories() {
        double menge;
        try {
            menge = Double.parseDouble((edMenge.getText().toString()));
        } catch (NumberFormatException e) {
            return;
        }
        if (menge == 0.0) {
            edKcalNew.setText("0");
            return;
        }

        String proteinEnteredStr = edNewProtein.getText().toString();
        String fatEnteredStr = edNewFat.getText().toString();
        String carbsEnteredStr = edNewCarbs.getText().toString();

        if (proteinEnteredStr.equals("0") && fatEnteredStr.equals("0") && carbsEnteredStr.equals("0")) {
            Log.d(logTag, "entered = 0");
            return;
        }

        int fat_entered = 0;
        int carbs_entered = 0;
        int protein_entered = 0;

        String result = "";

        // multiply by 10 to get from milligrams to grams per 100 grams
        try {
            fat_entered += Double.parseDouble(fatEnteredStr) * menge * 10.0;
            result += "F: " + String.format("%.1f", ((double) fat_entered) / 1000.0);
        } catch (NumberFormatException e) {
        }

        try {
            carbs_entered += Double.parseDouble(carbsEnteredStr) * menge * 10.0;
            result += "  C: " + String.format("%.1f", ((float) carbs_entered) / 1000.0);
        } catch (NumberFormatException e) {
        }

        try {
            protein_entered += Double.parseDouble(proteinEnteredStr) * menge * 10.0;
            result += "  P: " + String.format("%.1f", ((float) protein_entered) / 1000.0);
        } catch (NumberFormatException e) {
        }

        int kCal = ((protein_entered + carbs_entered) * 4 + fat_entered * 9) / 1000;

        result += String.format(" (%d kcal)", kCal);
        edKcalNew.setText(result);
    }

    private void onEatClicked() {
        double menge;
        try {
            menge = Double.parseDouble((edMenge.getText().toString()));
        } catch (NumberFormatException e) {
            return;
        }
        if (menge == 0.0) {
            return;
        }

        calcTempCalories();

        String fatEntered = edNewFat.getText().toString();
        String carbsEntered = edNewCarbs.getText().toString();
        String proteinEntered = edNewProtein.getText().toString();

        edNewFat.setText("0");
        edNewCarbs.setText("0");
        edNewProtein.setText("0");
        edMenge.setText("100");

        if (proteinEntered.equals("0") && fatEntered.equals("0") && carbsEntered.equals("0")) {
            return;
        }

        // multiply by 10 to get from milligrams to grams per 100 grams
        try {
            m_fat += Double.parseDouble(fatEntered) * menge * 10.0;
        } catch (NumberFormatException e) {
        }

        try {
            m_carbs += Double.parseDouble(carbsEntered) * menge * 10.0;
        } catch (NumberFormatException e) {
        }

        try {
            m_protein += Double.parseDouble(proteinEntered) * menge * 10.0;
        } catch (NumberFormatException e) {
        }

        View view = MainActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        setActualValues();
    }

    private void setActualValues() {
        int kCal = ((m_protein + m_carbs) * 4 + m_fat * 9) / 1000;
        int fatGrams = m_fat / 1000;
        int carbsGrams = m_carbs / 1000;
        int proteinGrams = m_protein / 1000;  // e.g. protein=120345 -> proteinGrams = 120

        int fatRemainingMillis = (m_fat - 1000 * fatGrams);
        int carbsRemainingMillis = (m_carbs - 1000 * carbsGrams);
        int proteinRemainingMillis = (m_protein - 1000 * proteinGrams);

        edActualFat.setText("" + fatGrams + "," + String.format("%03d", fatRemainingMillis));
        edActualCarbs.setText("" + carbsGrams + "," + String.format("%03d", carbsRemainingMillis));
        edActualProtein.setText("" + proteinGrams + "," + String.format("%03d", proteinRemainingMillis));
        edActualkCal.setText("" + kCal);
        m_valuesChanged = true;
    }


    private void clearValues() {
        new AlertDialog.Builder(this)
                .setMessage("Alles auf 0?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        m_protein = m_fat = m_carbs = 0;
                        setActualValues();
                    }
                }).create().show();
    }

    private void saveMeal() {
        edName.clearFocus();
        edNewFat.clearFocus();
        edNewCarbs.clearFocus();
        edNewProtein.clearFocus();

        MealValues newMv = new MealValues();
        String newMealName = edName.getText().toString();
        if (newMealName.isEmpty()) {
            return;
        }

        newMv.name = newMealName;
        newMv.fat = edNewFat.getText().toString();
        newMv.carbs = edNewCarbs.getText().toString();
        newMv.protein = edNewProtein.getText().toString();

        m_mealValues.add(newMv);
    }

    private void buildYaml() {
//        String resYaml = "";
//        for (int i = 0; i < len; ++i) {
//            if (newMealName.equals(m_mealsNames[i])) {
//                return;
//            }
//            newMealsNames[i] = m_mealsNames[i];
//            newMealsF[i] = m_mealsF[i];
//            newMealsC[i] = m_mealsC[i];
//            newMealsP[i] = m_mealsP[i];
//
//            resYaml += "- 'n': " + m_mealsNames[i] + "\n";
//            resYaml += "  f: '" + m_mealsF[i] + "'\n";
//            resYaml += "  c: '" + m_mealsC[i] + "'\n";
//            resYaml += "  p: '" + m_mealsP[i] + "'\n";
//        }
    }

    private void onSelectMealClicked() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setSingleChoiceItems(getMealNames(), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int id) {
                m_mealChoice = id;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int id) {
                if (m_mealChoice != -1) {
                    edName.setText(m_mealValues.get(m_mealChoice).name);
                    edNewFat.setText(m_mealValues.get(m_mealChoice).fat);
                    edNewCarbs.setText(m_mealValues.get(m_mealChoice).carbs);
                    edNewProtein.setText(m_mealValues.get(m_mealChoice).protein);
                }
            }
        });
        builder.setNegativeButton("Nullen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                edName.setText("");
                edNewFat.setText("0");
                edNewCarbs.setText("0");
                edNewProtein.setText("0");
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveValues() {
        SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("fat", m_fat);
        editor.putInt("carbs", m_carbs);
        editor.putInt("protein", m_protein);
        editor.commit();
    }

    public void onOptionClicked(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.menu_main);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reset:
                clearValues();
                return true;
            case R.id.action_savemeal:
                saveMeal();
                return true;
        }

        return false;
    }
}
