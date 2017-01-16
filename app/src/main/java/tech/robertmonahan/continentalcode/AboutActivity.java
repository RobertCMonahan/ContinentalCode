package tech.robertmonahan.continentalcode;



import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean darkMode = sharedPref.getBoolean("key_dark_mode", false);
        if (darkMode){ // if dark mode is selected
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.DefaultTheme);
        }

        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_about);

        Button licence_button = (Button) findViewById(R.id.license_button);

        licence_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // Would you like to Know more
                        // https://developer.android.com/reference/android/app/AlertDialog.Builder.html
                        AlertDialog.Builder builder_license = new AlertDialog.Builder(new ContextThemeWrapper(AboutActivity.this, R.style.AppCompatAlertDialogStyle));
                        builder_license.setMessage("MIT License\n\nCopyright © 2016 Robert Monahan\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
                        builder_license.setCancelable(true);

                        builder_license.setNegativeButton(
                                "Close",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert_license = builder_license.create();
                        alert_license.show();

                    }
                }
        );

    }



    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    // **** Action Bar Up Button **** //
    /*
     * Would you like to know more?
     * http://www.vogella.com/tutorials/AndroidActionBar/article.html#navigation-via-the-application-icon
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // handle the click for the up button (back arrow at the action bar)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // **** Action Bar Back Button **** //
}
