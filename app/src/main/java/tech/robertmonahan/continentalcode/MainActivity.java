package tech.robertmonahan.continentalcode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import static tech.robertmonahan.continentalcode.R.id.settings_button;


public class MainActivity extends AppCompatActivity {

    // create private class thing for each element that changes
    private EditText input_message;
    private TextView output_message;
    private static final boolean[] in_settings = {false};


    // **** Action Bar **** //
    /*
     * Settings icon button in action bar
     * Would You Like to Know More?
     * http://www.vogella.com/tutorials/AndroidActionBar/article.html
     * https://stackoverflow.com/questions/17914017/android-4-3-menu-item-showasaction-always-ignored#17914095
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // onClick for clicking settings button
    // Would You Like to Know More? http://www.vogella.com/tutorials/AndroidActionBar/article.html#creating-actions-in-the-toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case settings_button:

                // end the main activity
                in_settings[0] = true; // stop handler loop
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                finish(); // stop main activity
                startActivity(mainActivityIntent); // restart main activity so back key has something to come back to


                // start the settings activity
                // Would You Like to Know More? http://www.androidhive.info/2011/08/how-to-switch-between-activities-in-android/
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    // **** Action Bar **** //



    // **** Body **** //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get settings
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean darkMode = sharedPref.getBoolean("key_dark_mode", false);
        // set theme before onCreate
        if (darkMode){ // if dark mode is selected
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.DefaultTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * set default prefs
         * Would you like to know more?
         * https://developer.android.com/guide/topics/ui/settings.html#Defaults
         * http://www.vogella.com/tutorials/AndroidFileBasedPersistence/article.html
         */
        PreferenceManager.setDefaultValues(this, R.xml.pref_all, true);

        // get references
        input_message = (EditText) findViewById(R.id.input_message);
        output_message = (TextView) findViewById(R.id.output_message);
        final ImageButton play_pause_button = (ImageButton) findViewById(R.id.play_pause_button);
        final ImageView flashing_rectangle = (ImageView) findViewById(R.id.flashing_rectangle);
        flashing_rectangle.setEnabled(false);

        in_settings[0] = false; // user is no longer in settings

        // Create beep sounds for all speeds
        //fast
        final MediaPlayer dotSoundFast = MediaPlayer.create(this, R.raw.beep0111);
        final MediaPlayer dashSoundFast = MediaPlayer.create(this, R.raw.beep0333);
        // medium (default
        final MediaPlayer dotSoundMedium = MediaPlayer.create(this, R.raw.beep0333);
        final MediaPlayer dashSoundMedium = MediaPlayer.create(this, R.raw.beep1000);
        // slow
        final MediaPlayer dotSoundSlow = MediaPlayer.create(this, R.raw.beep0500);
        final MediaPlayer dashSoundSlow = MediaPlayer.create(this, R.raw.beep1500);
        // very slow
        final MediaPlayer dotSoundVerySlow = MediaPlayer.create(this, R.raw.beep1000);
        final MediaPlayer dashSoundVerySlow = MediaPlayer.create(this, R.raw.beep3000);

        final String[] playStop = {"stop"}; //state of the handler loop

        // start morse code
        play_pause_button.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View view) {
                        if (playStop[0].equals("stop")){
                            playStop[0] = "play";
                            play_pause_button.setBackgroundResource(R.drawable.pause);
                        } else if (playStop[0].equals("play")){
                            playStop[0] = ("stop");
                            play_pause_button.setBackgroundResource(R.drawable.play);
                        }

                        // get user preferences
                        final String speed = sharedPref.getString("key_speed", "");
                        final Boolean light = sharedPref.getBoolean("key_light", true);
                        final Boolean sound = sharedPref.getBoolean("key_sound", true);

                        // Get the correct speed and set it
                        final int oneUnit;
                        final MediaPlayer dotSound;
                        final MediaPlayer dashSound;

                        switch (speed) {
                            case "Fast":
                                oneUnit = 100;
                                dotSound = dotSoundFast;
                                dashSound = dashSoundFast;
                                break;
                            case "Medium":
                                oneUnit = 320;
                                dotSound = dotSoundMedium;
                                dashSound = dashSoundMedium;
                                break;
                            case "Slow":
                                oneUnit = 500;
                                dotSound = dotSoundSlow;
                                dashSound = dashSoundSlow;
                                break;
                            case "Very Slow":
                                oneUnit = 1000;
                                dotSound = dotSoundVerySlow;
                                dashSound = dashSoundVerySlow;
                                break;
                            default:
                                oneUnit = 320;
                                dotSound = dotSoundMedium;
                                dashSound = dashSoundMedium;
                                break;
                        }

                        // encode the message in morse
                        final String encoded_message = encode();
                        output_message.setText(encoded_message);

                        /*
                         * This is a Handler Loop. It's purpose is to loop though my tasks but I can control the timing of what is happening
                         * and it has a switch so that the light is always turned off for x amount of time and then it is switched back and
                         * the other part of the loop continues
                         *
                         * loopNumber MUST be final but because it's being used to control the loop I need to change it from inside the loop
                         * so the it's a one element array so that the element can be changed
                         * Would You Like to Know More?
                         * http://www.java-forums.org/new-java/23587-local-variable-tf1-accessed-within-inner-class-needs-declared-final.html
                         */
                        final Handler handler = new Handler();
                        final int[] loopNumber = {0}; // this is equivalent to 'int i=0;' in a normal for loop
                        final int[] timeBetweenLoop = {0}; // sets the time delay between each loop
                        final int[] loopSwitch = {1};

                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                if (loopSwitch[0] == 1) {
                                    char currentChar = encoded_message.charAt(loopNumber[0]);
                                    if (currentChar == '.') {
                                        if (sound){
                                            dotSound.start();
                                        }
                                        if (light) {
                                            lightOn();
                                        }
                                        //ShapeDrawable drawable = new ShapeDrawable();
                                        //flashing_rectangle.getPaint().setColor(getResources().getColor(R.color.flashOn));
                                        flashing_rectangle.setEnabled(true);
                                        //flashing_rectangle.setBackgroundColor(Color.parseColor(String.valueOf(R.color.flashOn)));
                                        timeBetweenLoop[0] = oneUnit;


                                    } else if (currentChar == '-') {
                                        if (sound){
                                            dashSound.start();
                                        }
                                        if (light) {
                                            lightOn();
                                        }
                                        flashing_rectangle.setEnabled(true);
                                        timeBetweenLoop[0] = 3 * oneUnit;

                                    } else if (currentChar == ' ') {
                                        timeBetweenLoop[0] = 2 * oneUnit; // space between letters

                                    } else if (currentChar == '/') {
                                        timeBetweenLoop[0] = 2 * oneUnit; // space between words

                                    } else {
                                        timeBetweenLoop[0] = 0;
                                    }

                                    loopNumber[0] = loopNumber[0] + 1; // this is equivalent to 'i++;' in a normal for loop
                                    loopSwitch[0] = 0; // switch to the light off

                                } else { // switch off light (break between dots and dashes) this does not increase the loop number
                                    if (light) {
                                        lightOff();

                                    }
                                    flashing_rectangle.setEnabled(false);
                                    timeBetweenLoop[0] = oneUnit; // space between parts of the same letters
                                    loopSwitch[0] = 1; // switch back to light on
                                }
                                handler.postDelayed(this, timeBetweenLoop[0]); // loops the runnable
                                if ((loopNumber[0] >= encoded_message.length()) || ( playStop[0].equals("stop")) || (in_settings[0])) {
                                    // when the message is done or
                                    // when the user presses the play pause button while the loop is running or
                                    // or the user goes to settings
                                    if (light) { // turn off light
                                        lightOff();
                                    }
                                    flashing_rectangle.setEnabled(false); // turn off rectangle
                                    handler.removeCallbacks(this); // breaks the loop

                                    playStop[0] = ("stop"); // set back to stop
                                    play_pause_button.setBackgroundResource(R.drawable.play); // change button image back to play button
                                }
                            }
                        };
                        handler.post(task);
                    }
                }
        );
    }

    // **** Body **** //



    // **** Utils **** //

    protected void lightOn(){
            /* flashlight code shamelessly taken from stackoverflow
             * Titled 'In API 23 or Higher (Android M, 6.0)' by Jack the Ripper
             * https://stackoverflow.com/questions/6068803/how-to-turn-on-camera-flash-light-programmatically-in-android/40748375#40748375
             */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId; // Usually back camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, true);   //Turn ON
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


    }

    protected void lightOff(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId; // Usually back camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, false); //Turn OFF
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    protected String getMessage(){
        // grab input message
        return input_message.getText().toString();
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected String encode(){
        String sourceMessage = getMessage();

        HashMap<Character, String> encodeDict = new HashMap<>(86);
        // each item in encodeDict is the morse code version of a letter with a space indicating the end of the letter

        // Alphabet
        encodeDict.put('A', "⋅- ");
        encodeDict.put('B', "-⋅⋅⋅ ");
        encodeDict.put('C', "-⋅-⋅ ");
        encodeDict.put('D', "-⋅⋅ ");
        encodeDict.put('E', "⋅ ");
        encodeDict.put('F', "⋅⋅-⋅ ");
        encodeDict.put('G', "--⋅ ");
        encodeDict.put('H', "⋅⋅⋅⋅ ");
        encodeDict.put('I', "⋅⋅ ");
        encodeDict.put('J', "⋅--- ");
        encodeDict.put('K', "-⋅- ");
        encodeDict.put('L', "⋅-⋅⋅ ");
        encodeDict.put('M', "-- ");
        encodeDict.put('N', "-⋅ ");
        encodeDict.put('O', "--- ");
        encodeDict.put('P', "⋅--⋅ ");
        encodeDict.put('Q', "--⋅- ");
        encodeDict.put('R', "⋅-⋅ ");
        encodeDict.put('S', "⋅⋅⋅ ");
        encodeDict.put('T', "- ");
        encodeDict.put('U', "⋅⋅- ");
        encodeDict.put('V', "⋅⋅⋅- ");
        encodeDict.put('W', "⋅-- ");
        encodeDict.put('X', "-⋅⋅- ");
        encodeDict.put('Y', "-⋅-- ");
        encodeDict.put('Z', "--⋅⋅ ");
        // Extended Alphabet
        encodeDict.put('Ä', "⋅-⋅- ");
        encodeDict.put('Æ', "⋅-⋅- ");
        encodeDict.put('Ą', "⋅-⋅- ");
        encodeDict.put('Á', "⋅--⋅- ");
        encodeDict.put('Å', "⋅--⋅- ");
        encodeDict.put('Ĉ', "-⋅-⋅⋅ ");
        encodeDict.put('Ć', "-⋅-⋅⋅ ");
        encodeDict.put('Ç', "-⋅-⋅⋅ ");
        encodeDict.put('Ĥ', "---- "); //Also CH
        encodeDict.put('Š', "---- ");
        encodeDict.put('Đ', "⋅⋅-⋅⋅ ");
        encodeDict.put('Ę', "⋅⋅-⋅⋅ ");
        encodeDict.put('É', "⋅⋅-⋅⋅ ");
        encodeDict.put('È', "⋅-⋅⋅- ");
        encodeDict.put('Ł', "⋅-⋅⋅- ");
        encodeDict.put('Ĝ', "--⋅-⋅ ");
        encodeDict.put('Ĵ', "⋅---⋅ ");
        encodeDict.put('Ń', "--⋅-- ");
        encodeDict.put('Ñ', "--⋅-- ");
        encodeDict.put('Ö', "---⋅ ");
        encodeDict.put('Ó', "---⋅ ");
        encodeDict.put('Ö', "---⋅ ");
        encodeDict.put('Ø', "---⋅ ");
        encodeDict.put('Ś', "⋅⋅⋅-⋅⋅⋅ ");
        encodeDict.put('Ŝ', "⋅⋅⋅-⋅ ");
        encodeDict.put('Þ', "⋅--⋅⋅ ");
        encodeDict.put('Ŭ', "⋅⋅-- ");
        encodeDict.put('Ü', "⋅⋅-- ");
        encodeDict.put('Ź', "--⋅⋅-⋅ ");
        encodeDict.put('Ż', "--⋅⋅- ");
        // Digits
        encodeDict.put('1', "⋅---- ");
        encodeDict.put('2', "⋅⋅--- ");
        encodeDict.put('3', "⋅⋅⋅-- ");
        encodeDict.put('4', "⋅⋅⋅⋅- ");
        encodeDict.put('5', "⋅⋅⋅⋅⋅ ");
        encodeDict.put('6', "-⋅⋅⋅⋅ ");
        encodeDict.put('7', "--⋅⋅⋅ ");
        encodeDict.put('8', "---⋅⋅ ");
        encodeDict.put('9', "----⋅ ");
        encodeDict.put('0', "----- ");
        // Punctuation
        encodeDict.put('.', "⋅-⋅-⋅- ");
        encodeDict.put(',', "--⋅⋅-- ");
        encodeDict.put(':', "---⋅⋅⋅ ");
        encodeDict.put('?', "⋅⋅--⋅⋅ ");
        encodeDict.put('\'', "⋅----⋅ ");
        encodeDict.put('-', "-⋅⋅⋅⋅- ");
        encodeDict.put('/', "-⋅⋅-⋅ ");
        encodeDict.put('"', "⋅-⋅⋅-⋅ ");
        encodeDict.put('@', "⋅--⋅-⋅ ");
        encodeDict.put('=', "-⋅⋅⋅- ");
        encodeDict.put('!', "−⋅−⋅−− ");
        encodeDict.put('(', "-⋅--⋅ ");
        encodeDict.put(')', "-⋅--⋅- ");
        encodeDict.put('&', "⋅-⋅⋅⋅ ");
        encodeDict.put(';', "-⋅-⋅-⋅ ");
        encodeDict.put('+', "⋅-⋅-⋅ ");
        encodeDict.put('_', "⋅⋅--⋅- ");
        encodeDict.put('$', "⋅⋅⋅-⋅⋅- ");
        encodeDict.put('Ŝ', "⋅⋅⋅-⋅ ");
        // a space (end of word)
        encodeDict.put(' ', "  ");

        sourceMessage = sourceMessage.toUpperCase();

        StringBuilder sb =  new StringBuilder();

        for (char ch : sourceMessage.toCharArray()) {
            // for each encoded letter in the message replace with the decoded letter and append to sb
            if (encodeDict.containsKey(ch)) {
                sb.append(encodeDict.get(ch));
            } else {
                sb.append("\u25AF");
            }
        }

        return sb.toString();
    }
    // **** Utils **** //
}

