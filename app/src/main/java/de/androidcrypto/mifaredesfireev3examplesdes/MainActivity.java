package de.androidcrypto.mifaredesfireev3examplesdes;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private com.google.android.material.textfield.TextInputEditText output, errorCode;
    private com.google.android.material.textfield.TextInputLayout errorCodeLayout;

    /**
     * section for general workflow
     */

    private LinearLayout llGeneralWorkflow;
    private Button tagVersion;

    /**
     * section for application handling
     */
    private LinearLayout llApplicationHandling;
    private Button applicationList, applicationCreate, applicationSelect;
    private com.google.android.material.textfield.TextInputEditText numberOfKeys, applicationId, applicationSelected;
    private byte[] selectedApplicationId = null;

    /**
     * section for standard file handling
     */

    private LinearLayout llStandardFile;
    private Button fileList, fileSelect, fileStandardCreate, fileStandardWrite, fileStandardRead;
    private com.shawnlin.numberpicker.NumberPicker npFileId;

    private com.google.android.material.textfield.TextInputEditText fileSelected;
    private com.google.android.material.textfield.TextInputEditText fileSize, fileData;
    private String selectedFileId = "";
    private int selectedFileSize;
    private FileSettings selectedFileSettings;

    private int selectedFileKeyRW, selectedFileKeyCar, selectedFileKeyR, selectedFileKeyW; // todo work on this


    /**
     * section for authentication
     */

    private Button authKeyD0, authKeyD1, authKeyD2, authKeyD3, authKeyD4;

    /**
     * section for key handling
     */

    private Button changeKeyD2, changeKeyD3, changeKeyD4;


    private byte KEY_NUMBER_USED_FOR_AUTHENTICATION; // the key number used for a successful authentication
    private byte[] SESSION_KEY_DES; // filled in authenticate, simply the first (leftmost) 8 bytes of SESSION_KEY_TDES
    private byte[] SESSION_KEY_TDES; // filled in authenticate
    private final byte[] DES_DEFAULT_KEY = new byte[8]; // 8 zero bytes


    private final byte[] DES_KEY_DEFAULT = new byte[8]; // 8 zero bytes
    private final byte[] DES_KEY_D0_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // key number 0 is the application master key
    private final byte[] DES_KEY_D1_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // key number 1 is for read&write access keys
    private final byte[] DES_KEY_D2_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // key number 2 is for change access keys
    private final byte[] DES_KEY_D3_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // key number 3 is for read access
    private final byte[] DES_KEY_D4_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // key number 4 is for write access
    private final byte[] DES_KEY_D0 = Utils.hexStringToByteArray("E000000000000000"); // key number 0 is the application master key
    private final byte[] DES_KEY_D1 = Utils.hexStringToByteArray("E100000000000000"); // key number 1 is for read&write access keys
    private final byte[] DES_KEY_D2 = Utils.hexStringToByteArray("E200000000000000"); // key number 2 is for change access keys
    private final byte[] DES_KEY_D3 = Utils.hexStringToByteArray("E300000000000000"); // key number 3 is for read access
    private final byte[] DES_KEY_D4 = Utils.hexStringToByteArray("E400000000000000"); // key number 4 is for write access
    private final byte DES_KEY_D0_NUMBER = (byte) 0x00;
    private final byte DES_KEY_D1_NUMBER = (byte) 0x01;
    private final byte DES_KEY_D2_NUMBER = (byte) 0x02;
    private final byte DES_KEY_D3_NUMBER = (byte) 0x03;
    private final byte DES_KEY_D4_NUMBER = (byte) 0x04;



    // old routines
    private Button fileStandardCreateOld;

    // constants
    public static final byte GET_VERSION_INFO = (byte) 0x60;
    private static final byte GET_ADDITIONAL_FRAME = (byte) 0xAF;


    private final byte[] MASTER_APPLICATION_IDENTIFIER = new byte[3]; // '00 00 00'
    private final byte[] MASTER_APPLICATION_KEY_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY = Utils.hexStringToByteArray("EE00000000000000");
    private final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    private final byte[] APPLICATION_ID_DES = Utils.hexStringToByteArray("B1B2B3");
    private final byte[] APPLICATION_KEY_MASTER_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_MASTER = Utils.hexStringToByteArray("E000000000000000");
    private final byte APPLICATION_KEY_MASTER_NUMBER = (byte) 0x00;
    private final byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks
    private final byte KEY_NUMBER_RW = (byte) 0x01;
    private final byte[] APPLICATION_KEY_RW_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_RW = Utils.hexStringToByteArray("E100000000000000");
    private final byte APPLICATION_KEY_RW_NUMBER = (byte) 0x01;
    private final byte[] APPLICATION_KEY_CAR_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_CAR = Utils.hexStringToByteArray("E200000000000000");
    private final byte APPLICATION_KEY_CAR_NUMBER = (byte) 0x02;

    private final byte[] APPLICATION_KEY_R_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_R = Utils.hexStringToByteArray("E300000000000000");
    private final byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;

    private final byte[] APPLICATION_KEY_W_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    //private final byte[] APPLICATION_KEY_W = Utils.hexStringToByteArray("B400000000000000");
    private final byte[] APPLICATION_KEY_W = Utils.hexStringToByteArray("E400000000000000");
    private final byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;

    private final byte STANDARD_FILE_NUMBER = (byte) 0x01;


    // Status codes (Section 3.4)
    private static final byte OPERATION_OK = (byte) 0x00;
    private static final byte PERMISSION_DENIED = (byte) 0x9D;
    private static final byte AUTHENTICATION_ERROR = (byte) 0xAE;
    private static final byte ADDITIONAL_FRAME = (byte) 0xAF;

    int COLOR_GREEN = Color.rgb(0,255,0);
    int COLOR_RED = Color.rgb(255,0,0);

    // variables for NFC handling

    private NfcAdapter mNfcAdapter;
    private CommunicationAdapter adapter;
    private IsoDep isoDep;
    private byte[] tagIdByte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        output = findViewById(R.id.etOutput);
        errorCode = findViewById(R.id.etErrorCode);
        errorCodeLayout = findViewById(R.id.etErrorCodeLayout);

        // general workflow
        tagVersion = findViewById(R.id.btnGetTagVersion);

        // application handling
        llApplicationHandling = findViewById(R.id.llApplications);
        applicationList = findViewById(R.id.btnListApplications);
        applicationCreate = findViewById(R.id.btnCreateApplication);
        applicationSelect = findViewById(R.id.btnSelectApplication);
        applicationSelected = findViewById(R.id.etSelectedApplicationId);
        numberOfKeys = findViewById(R.id.etNumberOfKeys);
        applicationId = findViewById(R.id.etApplicationId);

        // standard file handling
        llStandardFile = findViewById(R.id.llStandardFile);
        fileList = findViewById(R.id.btnListFiles);
        fileSelect = findViewById(R.id.btnSelectFile);

        fileStandardCreate = findViewById(R.id.btnCreateStandardFile);
        fileStandardWrite = findViewById(R.id.btnWriteStandardFile);
        fileStandardRead = findViewById(R.id.btnReadStandardFile);
        npFileId = findViewById(R.id.npStandardFileId);
        fileSelected = findViewById(R.id.etSelectedFileId);

        fileSize = findViewById(R.id.etFileSize);
        fileData = findViewById(R.id.etFileData);

        // authentication handling
        authKeyD0 = findViewById(R.id.btnAuthD0);
        authKeyD1 = findViewById(R.id.btnAuthD1);
        authKeyD3 = findViewById(R.id.btnAuthD3);
        authKeyD4 = findViewById(R.id.btnAuthD4);

        // key handling
        changeKeyD2 = findViewById(R.id.btnChangeKeyD2);
        changeKeyD3 = findViewById(R.id.btnChangeKeyD3);
        changeKeyD4 = findViewById(R.id.btnChangeKeyD4);


        // hide soft keyboard from showing up on startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //allLayoutsInvisible(); // default
        applicationId.setText(Utils.bytesToHexNpe(APPLICATION_ID_DES).toUpperCase());

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


        /**
         * section for general workflow
         */

        tagVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the tag version data
                clearOutputFields();
                VersionInfo versionInfo = null;
                try {
                    versionInfo = getVersionInfo(output);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "success in getting tagVersion", COLOR_GREEN);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getTagVersion Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                }
                if (versionInfo != null) {
                    writeToUiAppend(output, versionInfo.dump());
                }
            }
        });

        /**
         * section for applications
         */
        applicationList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get application ids
                clearOutputFields();
                byte[] responseData = new byte[2];
                List<byte[]> applicationIdList = getApplicationIdsList(output, responseData);
                //writeToUiAppend(errorCode, "getApplicationIdsList: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                if (applicationIdList != null) {
                    for (int i = 0; i < applicationIdList.size(); i++) {
                        writeToUiAppend(output, "entry " + i + " app id : " + Utils.bytesToHex(applicationIdList.get(i)));
                    }
                } else {
                    //writeToUiAppend(errorCode, "getApplicationIdsList: returned NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList returned NULL", COLOR_RED);
                }
            }
        });

        applicationCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                // get the input and sanity checks
                clearOutputFields();
                byte numberOfKeysByte = Byte.parseByte(numberOfKeys.getText().toString());
                byte[] applicationIdentifier = Utils.hexStringToByteArray(applicationId.getText().toString());
                if (applicationIdentifier == null) {
                    //writeToUiAppend(errorCode, "you entered a wrong application ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong application ID", COLOR_RED);
                    return;
                }
                if (applicationIdentifier.length != 3) {
                    //writeToUiAppend(errorCode, "you did not enter a 6 hex string application ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you did not enter a 6 hex string application ID", COLOR_RED);
                    return;
                }
                byte[] responseData = new byte[2];
                boolean result = createApplicationPlainDes(output, applicationIdentifier, numberOfKeysByte, responseData);
                writeToUiAppend(output, "result of createAnApplication: " + result);
                //writeToUiAppend(errorCode, "createAnApplication: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAnApplication: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        applicationSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all applications and show them in a listview for selection
                clearOutputFields();
                byte[] responseData = new byte[2];
                List<byte[]> applicationIdList = getApplicationIdsList(output, responseData);
                //writeToUiAppend(errorCode, "getApplicationIdsList: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                if (applicationIdList != null) {
                    for (int i = 0; i < applicationIdList.size(); i++) {
                        // writeToUiAppend(output, "entry " + i + " app id : " + Utils.bytesToHex(applicationIdList.get(i)));
                    }
                } else {
                    //writeToUiAppend(errorCode, "getApplicationIdsList: returned NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList returned NULL", COLOR_RED);
                    return;
                }
                String[] applicationList = new String[applicationIdList.size()];
                for (int i = 0; i < applicationIdList.size(); i++) {
                    applicationList[i] = Utils.bytesToHex(applicationIdList.get(i));
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose an application");

                // add a list
                //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
                //builder.setItems(animals, new DialogInterface.OnClickListener() {
                builder.setItems(applicationList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you  selected nr " + which + " = " + applicationList[which]);
                        selectedApplicationId = Utils.hexStringToByteArray(applicationList[which]);
                        // now we run the command to select the application
                        byte[] responseData = new byte[2];
                        boolean result = selectApplicationDes(output, selectedApplicationId, responseData);
                        writeToUiAppend(output, "result of selectApplicationDes: " + result);
                        //writeToUiAppend(errorCode, "selectApplicationDes: " + Ev3.getErrorCode(responseData));
                        int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "selectApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                        applicationSelected.setText(applicationList[which]);
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        /**
         * section for authentication
         */

        authKeyD0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 0 (Application Master Key) with DEFAULT KEY
                clearOutputFields();
                SESSION_KEY_DES = new byte[8];
                SESSION_KEY_TDES = new byte[16];
                byte[] responseData = new byte[2];
                //byte keyId = (byte) 0x01; // we authenticate with keyId 0
                boolean result = authenticateApplicationDes(output, DES_KEY_D0_NUMBER, DES_KEY_D0_DEFAULT, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = DES_KEY_D0_NUMBER;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        authKeyD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 1 (CAR) with DEFAULT KEY
                clearOutputFields();
                SESSION_KEY_DES = new byte[8];
                SESSION_KEY_TDES = new byte[16];
                byte[] responseData = new byte[2];
                byte keyId = (byte) 0x01; // we authenticate with keyId 1
                boolean result = authenticateApplicationDes(output, keyId, DES_DEFAULT_KEY, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = keyId;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        authKeyD3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 3 (R) with DEFAULT KEY
                clearOutputFields();
                SESSION_KEY_DES = new byte[8];
                SESSION_KEY_TDES = new byte[16];
                byte[] responseData = new byte[2];
                //byte keyId = (byte) 0x01; // we authenticate with keyId 1
                boolean result = authenticateApplicationDes(output, DES_KEY_D3_NUMBER, DES_DEFAULT_KEY, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = DES_KEY_D3_NUMBER;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        authKeyD4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 4 (W) with DEFAULT KEY
                clearOutputFields();
                SESSION_KEY_DES = new byte[8];
                SESSION_KEY_TDES = new byte[16];
                byte[] responseData = new byte[2];
                //byte keyId = (byte) 0x01; // we authenticate with keyId 1
                boolean result = authenticateApplicationDes(output, DES_KEY_D4_NUMBER, DES_DEFAULT_KEY, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = DES_KEY_D4_NUMBER;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });


        /**
         * section for key handling
         */

        changeKeyD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this method will change the key number 2 (read access) from default to D200...
                clearOutputFields();
                byte[] responseData = new byte[2];
                byte KEY_NUMBER_TO_CHANGE = 2;

                boolean result = changeKeyDes(output, KEY_NUMBER_TO_CHANGE, DES_DEFAULT_KEY, DES_KEY_D2, responseData);
                writeToUiAppend(output, "result of changeKeyDes: " + result);
                //writeToUiAppend(errorCode, "createAnApplication: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKeyDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

            }
        });

        changeKeyD3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this method will change the key number 3 (read access) from default to D300...
                clearOutputFields();
                byte[] responseData = new byte[2];
                writeToUiAppend(output, "changeKeyDes: "
                        + Utils.byteToHex(DES_KEY_D3_NUMBER)
                        + " from " + printData("oldValue", DES_KEY_D3_DEFAULT)
                        + " to " + printData("newValue", DES_KEY_D3));
                boolean result = changeKeyDes(output, DES_KEY_D3_NUMBER, DES_DEFAULT_KEY, DES_KEY_D3, responseData);

                writeToUiAppend(output, "result of changeKeyDes: " + result);
                //writeToUiAppend(errorCode, "createAnApplication: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKeyDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

            }
        });

        changeKeyD4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this method will change the key number 4 (write access) from default to D400...
                clearOutputFields();
                byte[] responseData = new byte[2];
                writeToUiAppend(output, "changeKeyDes: "
                        + Utils.byteToHex(DES_KEY_D4_NUMBER)
                        + " from " + printData("oldValue", DES_KEY_D4_DEFAULT)
                        + " to " + printData("newValue", DES_KEY_D4));
                boolean result = changeKeyDes(output, DES_KEY_D4_NUMBER, DES_DEFAULT_KEY, DES_KEY_D4, responseData);

                writeToUiAppend(output, "result of changeKeyDes: " + result);
                //writeToUiAppend(errorCode, "createAnApplication: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKeyDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

            }
        });


        /**
         * section  for standard files
         */

        /*
        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the default DES key
                clearOutputFields();
                SESSION_KEY_DES = new byte[8];
                SESSION_KEY_TDES = new byte[16];
                byte[] responseData = new byte[2];
                byte keyId = (byte) 0x00; // we authenticate with keyId 0
                boolean result = authenticateApplicationDes(output, keyId, DES_DEFAULT_KEY, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = keyId;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

         */

        fileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // list all files in a selected application
                byte[] responseData = new byte[2];
                List<Byte> fileIdList = getFileIdsList(output, responseData);
                //writeToUiAppend(errorCode, "getFileIdsList: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFileIdsList: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

                if (fileIdList != null) {
                    for (int i = 0; i < fileIdList.size(); i++) {
                        byte fileId = fileIdList.get(i);
                        writeToUiAppend(output, "entry " + i + " file id : " + Utils.byteToHex(fileId));

                        // here we are reading the fileSettings
                        byte[] fileSettingsBytes = getFileSettings(output, fileId, responseData);
                        if ((fileSettingsBytes != null) & (fileSettingsBytes.length >= 7)) {
                            FileSettings fileSettings = new FileSettings(fileId, fileSettingsBytes);
                            writeToUiAppend(output, fileSettings.dump());
                            writeToUiAppend(output, "------------------");
                        }
                    }
                } else {
                    //writeToUiAppend(errorCode, "getFileIdsList: returned NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFileIdsList returned NULL", COLOR_RED);
                }

            }
        });

        fileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select a file in a selected application
                clearOutputFields();
                byte[] responseData = new byte[2];
                List<Byte> fileIdList = getFileIdsList(output, responseData);
                //writeToUiAppend(errorCode, "getFileIdsList: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFileIdsList: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                if (fileIdList != null) {
                    for (int i = 0; i < fileIdList.size(); i++) {
                        writeToUiAppend(output, "entry " + i + " file id : " + Utils.byteToHex(fileIdList.get(i)));
                    }
                } else {
                    //writeToUiAppend(errorCode, "getFileIdsList: returned NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFileIdsList returned NULL", COLOR_RED);
                    return;
                }
                String[] fileList = new String[fileIdList.size()];
                for (int i = 0; i < fileIdList.size(); i++) {
                    fileList[i] = Utils.byteToHex(fileIdList.get(i));
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a file");

                builder.setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you  selected nr " + which + " = " + fileList[which]);
                        selectedFileId = fileList[which];
                        // now we run the command to select the application
                        byte[] responseData = new byte[2];
                        //boolean result = selectDes(output, selectedApplicationId, responseData);
                        //writeToUiAppend(output, "result of selectApplicationDes: " + result);
                        //writeToUiAppend(errorCode, "selectApplicationDes: " + Ev3.getErrorCode(responseData));

                        // here we are reading the fileSettings
                        String outputString = fileList[which] + " ";
                        byte fileIdByte = Byte.parseByte(selectedFileId);
                        byte[] fileSettingsBytes = getFileSettings(output, fileIdByte, responseData);
                        if ((fileSettingsBytes != null) & (fileSettingsBytes.length >= 7)) {
                            selectedFileSettings = new FileSettings(fileIdByte, fileSettingsBytes);
                            outputString += "(" + selectedFileSettings.getFileTypeName();
                            selectedFileSize = selectedFileSettings.getFileSizeInt();
                            outputString += " size: " + selectedFileSize + ")";
                            writeToUiAppend(output, outputString);
                        }
                        fileSelected.setText(fileList[which]);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "file selected", COLOR_GREEN);
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        fileStandardCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new standard file
                // get the input and sanity checks
                clearOutputFields();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a limit of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                // this uses the numberPicker
                byte fileIdByte = (byte) (npFileId.getValue() & 0xFF);
                // this is done with an EditText
                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    //writeToUiAppend(errorCode, "you entered a wrong file ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                if (fileSizeInt != 32) {
                    //writeToUiAppend(errorCode, "you entered a wrong file size, 32 bytes allowed only");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file size, 32 bytes allowed only", COLOR_RED);
                    return;
                }
                byte[] responseData = new byte[2];
                boolean result = createStandardFile(output, fileIdByte, fileSizeInt, responseData);
                writeToUiAppend(output, "result of createAStandardFile: " + result + " ID: " + fileIdByte + " size: " + fileSizeInt);
                //writeToUiAppend(errorCode, "createAStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

            }
        });

        fileStandardWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected standard file in a selected application
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWrite = fileData.getText().toString();
                byte[] dataToWriteBytes = dataToWrite.getBytes(StandardCharsets.UTF_8);
                // here we are using testdata
                int fileSize = selectedFileSize;
                writeToUiAppend(output, "As the selected file has a size of " + fileSize + " we generate the same length as test data");
                dataToWriteBytes = Utils.generateTestData(fileSize);

                if (TextUtils.isEmpty(dataToWrite)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, printData("testdata", dataToWriteBytes));

                byte fileIdByte = Byte.parseByte(selectedFileId);

                byte[] responseData = new byte[2];
                //boolean result = writeToStandardFile(output, fileIdByte, dataToWrite.getBytes(StandardCharsets.UTF_8), responseData);
                boolean result = writeToStandardFile(output, fileIdByte, dataToWriteBytes, responseData);
                //writeToUiAppend(output, "result of writeToStandardFile: " + result + " ID: " + fileIdByte + " data: " + dataToWrite);
                writeToUiAppend(output, "result of writeToStandardFile: " + result + " to fileID: " + fileIdByte);
                //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeToStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        fileStandardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read from a preselected standard file
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                byte fileIdByte = Byte.parseByte(selectedFileId);
                byte[] responseData = new byte[2];
                byte[] result = readFromStandardFile(output, fileIdByte, responseData);
                //byte[] result = readFromStandardFileLimitedSize(output, fileIdByte, responseData);
                writeToUiAppend(output, "readFromStandardFile" + " ID: " + fileIdByte + printData(" data", result));
                writeToUiAppend(output, "readFromStandardFile" + " ID: " + fileIdByte + " data: " +  new String(result, StandardCharsets.UTF_8));
                //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

    }

    /**
     * section for general workflow
     */

    public VersionInfo getVersionInfo(TextView logTextView) throws Exception {
        byte[] bytes = sendRequest(logTextView, GET_VERSION_INFO);
        return new VersionInfo(bytes);
    }

    private byte[] sendRequest(TextView logTextView, byte command) throws Exception {
        return sendRequest(logTextView, command, null);
    }

    // todo take this as MASTER for sending commands to the card and receiving data
    private byte[] sendRequest(TextView logTextView, byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] recvBuffer = isoDep.transceive(wrapMessage(command, parameters));
        writeToUiAppend(logTextView, printData("sendRequest recvBuffer", recvBuffer));
        while (true) {
            if (recvBuffer[recvBuffer.length - 2] != (byte) 0x91) {
                throw new Exception("Invalid response");
            }

            output.write(recvBuffer, 0, recvBuffer.length - 2);

            byte status = recvBuffer[recvBuffer.length - 1];
            if (status == OPERATION_OK) {
                break;
            } else if (status == ADDITIONAL_FRAME) {
                recvBuffer = isoDep.transceive(wrapMessage(GET_ADDITIONAL_FRAME, null));
            } else if (status == PERMISSION_DENIED) {
                throw new AccessControlException("Permission denied");
            } else if (status == AUTHENTICATION_ERROR) {
                throw new AccessControlException("Authentication error");
            } else {
                throw new Exception("Unknown status code: " + Integer.toHexString(status & 0xFF));
            }
        }
        return output.toByteArray();
    }

    /**
     * section for authentication with DES
     */

    // if verbose = true all steps are printed out
    private boolean authenticateApplicationDes(TextView logTextView, byte keyId, byte[] key, boolean verbose, byte[] response) {
        try {
            writeToUiAppend(logTextView, "authenticateApplicationDes for keyId " + keyId + " and key " + Utils.bytesToHex(key));
            // do DES auth
            //String getChallengeCommand = "901a0000010000";
            //String getChallengeCommand = "9084000000"; // IsoGetChallenge
            byte[] getChallengeResponse = isoDep.transceive(wrapMessage((byte) 0x1a, new byte[]{(byte) (keyId & 0xFF)}));
            if (verbose)
                writeToUiAppend(logTextView, printData("getChallengeResponse", getChallengeResponse));
            // cf5e0ee09862d90391af
            // 91 af at the end shows there is more data

            byte[] challenge = Arrays.copyOf(getChallengeResponse, getChallengeResponse.length - 2);
            if (verbose) writeToUiAppend(logTextView, printData("challengeResponse", challenge));

            // Of course the rndA shall be a random number,
            // but we will use a constant number to make the example easier.
            //byte[] rndA = Utils.hexStringToByteArray("0001020304050607");
            byte[] rndA = Ev3.getRndADes();
            if (verbose) writeToUiAppend(logTextView, printData("rndA", rndA));

            // This is the default key for a blank DESFire card.
            // defaultKey = 8 byte array = [0x00, ..., 0x00]
            //byte[] defaultDESKey = Utils.hexStringToByteArray("0000000000000000");
            byte[] defaultDESKey = key.clone();
            byte[] IV = new byte[8];

            // Decrypt the challenge with default keybyte[] rndB = decrypt(challenge, defaultDESKey, IV);
            byte[] rndB = Ev3.decrypt(challenge, defaultDESKey, IV);
            if (verbose) writeToUiAppend(logTextView, printData("rndB", rndB));
            // Rotate left the rndB byte[] leftRotatedRndB = rotateLeft(rndB);
            byte[] leftRotatedRndB = Ev3.rotateLeft(rndB);
            if (verbose)
                writeToUiAppend(logTextView, printData("leftRotatedRndB", leftRotatedRndB));
            // Concatenate the RndA and rotated RndB byte[] rndA_rndB = concatenate(rndA, leftRotatedRndB);
            byte[] rndA_rndB = Ev3.concatenate(rndA, leftRotatedRndB);
            if (verbose) writeToUiAppend(logTextView, printData("rndA_rndB", rndA_rndB));

            // Encrypt the bytes of the last step to get the challenge answer byte[] challengeAnswer = encrypt(rndA_rndB, defaultDESKey, IV);
            IV = challenge;
            byte[] challengeAnswer = Ev3.encrypt(rndA_rndB, defaultDESKey, IV);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswer", challengeAnswer));

            IV = Arrays.copyOfRange(challengeAnswer, 8, 16);
                /*
                    Build and send APDU with the answer. Basically wrap the challenge answer in the APDU.
                    The total size of apdu (for this scenario) is 22 bytes:
                    > 0x90 0xAF 0x00 0x00 0x10 [16 bytes challenge answer] 0x00
                */
            byte[] challengeAnswerAPDU = new byte[22];
            challengeAnswerAPDU[0] = (byte) 0x90; // CLS
            challengeAnswerAPDU[1] = (byte) 0xAF; // INS
            challengeAnswerAPDU[2] = (byte) 0x00; // p1
            challengeAnswerAPDU[3] = (byte) 0x00; // p2
            challengeAnswerAPDU[4] = (byte) 0x10; // data length: 16 bytes
            challengeAnswerAPDU[challengeAnswerAPDU.length - 1] = (byte) 0x00;
            System.arraycopy(challengeAnswer, 0, challengeAnswerAPDU, 5, challengeAnswer.length);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerAPDU", challengeAnswerAPDU));

            /*
             * Sending the APDU containing the challenge answer.
             * It is expected to be return 10 bytes [rndA from the Card] + 9100
             */
            byte[] challengeAnswerResponse = isoDep.transceive(challengeAnswerAPDU);
            // response = channel.transmit(new CommandAPDU(challengeAnswerAPDU));
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerResponse", challengeAnswerResponse));
            byte[] challengeAnswerResp = Arrays.copyOf(challengeAnswerResponse, getChallengeResponse.length - 2);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerResp", challengeAnswerResp));

            /*
             * At this point, the challenge was processed by the card. The card decrypted the
             * rndA rotated it and sent it back.
             * Now we need to check if the RndA sent by the Card is valid.
             */// encrypted rndA from Card, returned in the last step byte[] encryptedRndAFromCard = response.getData();

            // Decrypt the rnd received from the Card.byte[] rotatedRndAFromCard = decrypt(encryptedRndAFromCard, defaultDESKey, IV);
            //byte[] rotatedRndAFromCard = decrypt(encryptedRndAFromCard, defaultDESKey, IV);
            byte[] rotatedRndAFromCard = Ev3.decrypt(challengeAnswerResp, defaultDESKey, IV);
            if (verbose)
                writeToUiAppend(logTextView, printData("rotatedRndAFromCard", rotatedRndAFromCard));

            // As the card rotated left the rndA,// we shall un-rotate the bytes in order to get compare it to our original rndA.byte[] rndAFromCard = rotateRight(rotatedRndAFromCard);
            byte[] rndAFromCard = Ev3.rotateRight(rotatedRndAFromCard);
            if (verbose) writeToUiAppend(logTextView, printData("rndAFromCard", rndAFromCard));
            writeToUiAppend(logTextView, "********** AUTH RESULT **********");
            //System.arraycopy(createApplicationResponse, 0, response, 0, createApplicationResponse.length);
            if (Arrays.equals(rndA, rndAFromCard)) {
                writeToUiAppend(logTextView, "Authenticated");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0x00};
                System.arraycopy(responseManual, 0, response, 0, 2);
                // now generate the session key
                SESSION_KEY_TDES = generateD40SessionKey(rndA, rndB); // this is a 16 bytes long key, but for D40 encryption (DES) we need 8 bytes only
                // as it is a single DES cryptography I'm using the first part of the SESSION_KEY_DES only
                SESSION_KEY_DES = Arrays.copyOf(SESSION_KEY_TDES, 8);
                return true;
            } else {
                writeToUiAppend(logTextView, "Authentication failed");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
                return false;
                //System.err.println(" ### Authentication failed. ### ");
                //log("rndA:" + toHexString(rndA) + ", rndA from Card: " + toHexString(rndAFromCard));
            }
            //writeToUiAppend(logTextView, "********** AUTH RESULT END **********");
            //return false;
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "authenticateApplicationDes transceive failed: " + e.getMessage());
            writeToUiAppend(logTextView, "authenticateApplicationDes transceive failed: " + Arrays.toString(e.getStackTrace()));
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
        }
        //System.arraycopy(createApplicationResponse, 0, response, 0, createApplicationResponse.length);
        return false;
    }

    private static byte[] generateD40SessionKey(byte[] randA, byte[] randB) {
        // this IS NOT described in the manual !!!
        /*
        RndA = 0000000000000000, RndB = A1A2A3A4A5A6A7A8
        sessionKey = 00000000A1A2A3A400000000A1A2A3A4 (16 byte
         */
        byte[] skey = new byte[16];
        System.arraycopy(randA, 0, skey, 0, 4);
        System.arraycopy(randB, 0, skey, 4, 4);
        System.arraycopy(randA, 0, skey, 8, 4);
        System.arraycopy(randB, 0, skey, 12, 4);
        return skey;
    }

    /**
     * section for standard file handling
     */

    private boolean createStandardFile(TextView logTextView, byte fileNumber, int fileSize, byte[] response) {
        // we create a standard file within the selected application
        byte createStandardFileCommand = (byte) 0xcd;
        // CD | File No | Comms setting byte | Access rights (2 bytes) | File size (3 bytes)
        byte commSettingsByte = 0; // plain communication without any encryption
                /*
                M0775031 DESFIRE
                Plain Communication = 0;
                Plain communication secured by DES/3DES MACing = 1;
                Fully DES/3DES enciphered communication = 3;
                 */
        //byte[] accessRights = new byte[]{(byte) 0xee, (byte) 0xee}; // should mean plain/free access without any keys
                /*
                There are four different Access Rights (2 bytes for each file) stored for each file within
                each application:
                - Read Access
                - Write Access
                - Read&Write Access
                - ChangeAccessRights
                 */
        // the application master key is key 0
        // here we are using key 3 for read and key 4 for write access access, key 1 has read&write access and key 2 has change rights !
        byte accessRightsRwCar = (byte) 0x12; // Read&Write Access & ChangeAccessRights
        byte accessRightsRW = (byte) 0x34; // Read Access & Write Access // read with key 3, write with key 4
        byte[] fileSizeArray = Utils.intTo3ByteArrayInversed(fileSize); // lsb
        byte[] createStandardFileParameters = new byte[7];
        createStandardFileParameters[0] = fileNumber;
        createStandardFileParameters[1] = commSettingsByte;
        createStandardFileParameters[2] = accessRightsRwCar;
        createStandardFileParameters[3] = accessRightsRW;
        System.arraycopy(fileSizeArray, 0, createStandardFileParameters, 4, 3);
        writeToUiAppend(logTextView, printData("createStandardFileParameters", createStandardFileParameters));
        byte[] createStandardFileResponse = new byte[0];
        try {
            createStandardFileResponse = isoDep.transceive(wrapMessage(createStandardFileCommand, createStandardFileParameters));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        System.arraycopy(returnStatusBytes(createStandardFileResponse), 0, response, 0, 2);
        writeToUiAppend(logTextView, printData("createStandardFileResponse", createStandardFileResponse));
        if (checkDuplicateError(createStandardFileResponse)) {
            writeToUiAppend(logTextView, "the file was not created as it already exists, proceed");
            return true;
        }
        if (checkResponse(createStandardFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    // note: we don't need to commit any write on Standard Files
    private boolean writeToStandardFile(TextView logTextView, byte fileNumber, byte[] data, byte[] response) {
        // some sanity checks to avoid any issues
        int fileSize = selectedFileSettings.getFileSizeInt();
        if (fileNumber < (byte) 0x00) return false;
        if (fileNumber > (byte) 0x0F) return false;
        if (data == null) return false;
        if (data.length == 0) return false;
        if (data.length > fileSize) return false;

        // write to file
        byte writeStandardFileCommand = (byte) 0x3d;
        int numberOfBytes = data.length;
        int offsetBytes = 0;
        //byte[] offset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00}; // no offset
        byte[] length = Utils.intTo3ByteArrayInversed(numberOfBytes);
        byte[] offset = Utils.intTo3ByteArrayInversed(offsetBytes);
        //byte[] length = new byte[]{(byte) (numberOfBytes & 0xFF), (byte) 0xf00, (byte) 0x00}; // 32 bytes
        byte[] writeStandardFileParameters = new byte[(7 + data.length)]; // if encrypted we need to append the CRC
        writeStandardFileParameters[0] = fileNumber;
        System.arraycopy(offset, 0, writeStandardFileParameters, 1, 3); // offset
        System.arraycopy(length, 0, writeStandardFileParameters, 4, 3); // length of data
        System.arraycopy(data, 0, writeStandardFileParameters, 7, data.length); // the data

        writeToUiAppend(logTextView, printData("writeStandardFileParameters", writeStandardFileParameters));
        byte[] writeStandardFileResponse = new byte[0];
        try {
            byte[] apdu = wrapMessage(writeStandardFileCommand, writeStandardFileParameters);
            writeStandardFileResponse = adapter.sendRequestChain(apdu);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        /*
        try {
            writeStandardFileResponse = isoDep.transceive(wrapMessage(writeStandardFileCommand, writeStandardFileParameters));
            writeToUiAppend(logTextView, printData("send APDU", wrapMessage(writeStandardFileCommand, writeStandardFileParameters)));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }

         */
        writeToUiAppend(logTextView, printData("writeStandardFileResponse", writeStandardFileResponse));
        System.arraycopy(returnStatusBytes(writeStandardFileResponse), 0, response, 0, 2);
        if (checkResponse(writeStandardFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean writeToStandardFileLimitedSize(TextView logTextView, byte fileNumber, byte[] data, byte[] response) {
        // some sanity checks to avoid any issues
        if (fileNumber < (byte) 0x00) return false;
        if (fileNumber > (byte) 0x0F) return false;
        if (data == null) return false;
        if (data.length == 0) return false;
        if (data.length > 32) return false; // todo work with maximum in fileSettings

        // write to file
        byte writeStandardFileCommand = (byte) 0x3d;
        int numberOfBytes = data.length;
        byte[] offset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00}; // no offset
        byte[] length = Utils.intTo3ByteArrayInversed(numberOfBytes);
        //byte[] length = new byte[]{(byte) (numberOfBytes & 0xFF), (byte) 0xf00, (byte) 0x00}; // 32 bytes
        byte[] writeStandardFileParameters = new byte[(7 + data.length)]; // if encrypted we need to append the CRC
        writeStandardFileParameters[0] = fileNumber;
        System.arraycopy(offset, 0, writeStandardFileParameters, 1, 3); // offset
        System.arraycopy(length, 0, writeStandardFileParameters, 4, 3); // length of data
        System.arraycopy(data, 0, writeStandardFileParameters, 7, data.length); // the data

        writeToUiAppend(logTextView, printData("writeStandardFileParameters", writeStandardFileParameters));
        byte[] writeStandardFileResponse = new byte[0];
        try {
            writeStandardFileResponse = isoDep.transceive(wrapMessage(writeStandardFileCommand, writeStandardFileParameters));
            writeToUiAppend(logTextView, printData("send APDU", wrapMessage(writeStandardFileCommand, writeStandardFileParameters)));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("writeStandardFileResponse", writeStandardFileResponse));
        System.arraycopy(returnStatusBytes(writeStandardFileResponse), 0, response, 0, 2);
        if (checkResponse(writeStandardFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    private byte[] readFromStandardFile(TextView logTextView, byte fileNumber, byte[] response) {
        // we read from a standard file within the selected application
        // as the file length is fixed we are reading with a constant length of 32

        int numberOfBytes = selectedFileSettings.getFileSizeInt();
        int offsetBytes = 0; // read from the beginning
        //int numberOfBytes = 32;
        byte readStandardFileCommand = (byte) 0xbd;
        //byte[] offset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00}; // no offset, read from the beginning
        byte[] offset = Utils.intTo3ByteArrayInversed(offsetBytes);
        byte[] length = Utils.intTo3ByteArrayInversed(numberOfBytes);
        byte[] readStandardFileParameters = new byte[7];
        readStandardFileParameters[0] = fileNumber;
        System.arraycopy(offset, 0, readStandardFileParameters, 1, 3);
        System.arraycopy(length, 0, readStandardFileParameters, 4, 3);
        writeToUiAppend(logTextView, printData("readStandardFileParameters", readStandardFileParameters));
        byte[] readStandardFileResponse = new byte[0];
        try {
            byte[] apdu = wrapMessage(readStandardFileCommand, readStandardFileParameters);
            readStandardFileResponse = adapter.send(apdu);
            writeToUiAppend(logTextView, printData("send APDU", wrapMessage(readStandardFileCommand, readStandardFileParameters)));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        /*
        try {
            readStandardFileResponse = isoDep.transceive(wrapMessage(readStandardFileCommand, readStandardFileParameters));
            writeToUiAppend(logTextView, printData("send APDU", wrapMessage(readStandardFileCommand, readStandardFileParameters)));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }

         */
        writeToUiAppend(logTextView, printData("readStandardFileResponse", readStandardFileResponse));
        //System.arraycopy(returnStatusBytes(readStandardFileResponse), 0, response, 0, 2);
        //return Arrays.copyOf(readStandardFileResponse, readStandardFileResponse.length - 2);
        System.arraycopy(adapter.getFullCode(), 0, response, 0, 2);

        // if the card responses more data than expected we truncate the data
        int expectedResponse = numberOfBytes - offsetBytes;
        if (readStandardFileResponse.length == expectedResponse) {
            return readStandardFileResponse;
        } else if (readStandardFileResponse.length > expectedResponse){
            // more data is provided - truncated
            return Arrays.copyOf(readStandardFileResponse, expectedResponse);
        } else {
            // less data is provided - we return as much as possible
            return readStandardFileResponse;
        }

        //return readStandardFileResponse;
        // apdu length: 13 data: 90bd0000070000000020000000
        // response length: 42 data: 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1fae2873a3bf1ef7809100

        // APDU length: 13 data: 90bd0000070000000020000000
        // Response length: 42 data: 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f3898f824d7f256f59100
    }

    private byte[] readFromStandardFileLimitedSize(TextView logTextView, byte fileNumber, byte[] response) {
        // we read from a standard file within the selected application
        // as the file length is fixed we are reading with a constant length of 32
        int numberOfBytes = 32;
        byte readStandardFileCommand = (byte) 0xbd;
        byte[] offset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00}; // no offset, read from the beginning
        byte[] length = Utils.intTo3ByteArrayInversed(numberOfBytes);
        byte[] readStandardFileParameters = new byte[7];
        readStandardFileParameters[0] = fileNumber;
        System.arraycopy(offset, 0, readStandardFileParameters, 1, 3);
        System.arraycopy(length, 0, readStandardFileParameters, 4, 3);
        writeToUiAppend(logTextView, printData("readStandardFileParameters", readStandardFileParameters));
        byte[] readStandardFileResponse = new byte[0];
        try {
            readStandardFileResponse = isoDep.transceive(wrapMessage(readStandardFileCommand, readStandardFileParameters));
            writeToUiAppend(logTextView, printData("send APDU", wrapMessage(readStandardFileCommand, readStandardFileParameters)));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        writeToUiAppend(logTextView, printData("readStandardFileResponse", readStandardFileResponse));
        System.arraycopy(returnStatusBytes(readStandardFileResponse), 0, response, 0, 2);
        return Arrays.copyOf(readStandardFileResponse, readStandardFileResponse.length - 2);
    }

    private List<Byte> getFileIdsList(TextView logTextView, byte[] response) {
        // get application ids
        List<Byte> fileIdList = new ArrayList<>();
        byte getFileIdsCommand = (byte) 0x6f;
        byte[] getFileIdsResponse = new byte[0];
        try {
            getFileIdsResponse = isoDep.transceive(wrapMessage(getFileIdsCommand, null));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        writeToUiAppend(logTextView, printData("getFileIdsResponse", getFileIdsResponse));

        // check that result if 0x9100 (success) or 0x91AF (success but more data)
        if ((!checkResponse(getFileIdsResponse)) && (!checkResponseMoreData(getFileIdsResponse))) {
            // something got wrong (e.g. missing authentication ?)
            writeToUiAppend(logTextView, "there was an unexpected response");
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        // if the read result is success 9100 we return the data received so far
        if (checkResponse(getFileIdsResponse)) {
            System.arraycopy(returnStatusBytes(getFileIdsResponse), 0, response, 0, 2);
            byte[] fileListBytes = Arrays.copyOf(getFileIdsResponse, getFileIdsResponse.length - 2);
            fileIdList = divideArrayToBytes(fileListBytes);
            return fileIdList;
        }
        if (checkResponseMoreData(getFileIdsResponse)) {
            writeToUiAppend(logTextView, "getFileIdsList: we are asked to grab more data from the card");
            byte[] fileListBytes = Arrays.copyOf(getFileIdsResponse, getFileIdsResponse.length - 2);
            fileIdList = divideArrayToBytes(fileListBytes);
            byte getMoreDataCommand = (byte) 0xaf;
            boolean readMoreData = true;
            try {
                while (readMoreData) {
                    try {
                        getFileIdsResponse = isoDep.transceive(wrapMessage(getMoreDataCommand, null));
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
                        return null;
                    }
                    writeToUiAppend(logTextView, printData("getFileIdsResponse", getFileIdsResponse));
                    if (checkResponse(getFileIdsResponse)) {
                        // now we have received all data
                        List<Byte> fileIdListTemp = new ArrayList<>();
                        System.arraycopy(returnStatusBytes(getFileIdsResponse), 0, response, 0, 2);
                        fileListBytes = Arrays.copyOf(getFileIdsResponse, getFileIdsResponse.length - 2);
                        fileIdListTemp = divideArrayToBytes(fileListBytes);
                        readMoreData = false; // end the loop
                        fileIdList.addAll(fileIdListTemp);
                        return fileIdListTemp;
                    }
                    if (checkResponseMoreData(getFileIdsResponse)) {
                        // some more data will follow, store temp data
                        List<Byte> fileIdListTemp = new ArrayList<>();
                        fileListBytes = Arrays.copyOf(getFileIdsResponse, getFileIdsResponse.length - 2);
                        fileIdListTemp = divideArrayToBytes(fileListBytes);
                        fileIdList.addAll(fileIdListTemp);
                        readMoreData = true;
                    }
                } // while (readMoreData) {
            } catch (Exception e) {
                writeToUiAppend(logTextView, "Exception failure: " + e.getMessage());
            } // try
        }
        return null;
    }

    // todo work on this
    private byte[] getFileSettings(TextView logTextView, byte fileNumber, byte[] response) {
        byte getFileSettingsCommand = (byte) 0xf5;
        byte[] getFileSettingsParameters = new byte[1];
        getFileSettingsParameters[0] = fileNumber;
        byte[] getFileSettingsResponse;
        try {
            getFileSettingsResponse = isoDep.transceive(wrapMessage(getFileSettingsCommand, getFileSettingsParameters));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        writeToUiAppend(logTextView, printData("getFileSettingsResponse", getFileSettingsResponse));
        System.arraycopy(returnStatusBytes(getFileSettingsResponse), 0, response, 0, 2);
        if (checkResponse(getFileSettingsResponse)) {
            return getFileSettingsResponse;
        } else {
            return null;
        }
    }

    /**
     * section for application handling
     */

    private List<byte[]> getApplicationIdsList(TextView logTextView, byte[] response) {
        // get application ids
        List<byte[]> applicationIdList = new ArrayList<>();
        byte getApplicationIdsCommand = (byte) 0x6a;
        byte[] getApplicationIdsResponse = new byte[0];
        try {
            getApplicationIdsResponse = isoDep.transceive(wrapMessage(getApplicationIdsCommand, null));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            return null;
        }
        writeToUiAppend(logTextView, printData("getApplicationIdsResponse", getApplicationIdsResponse));
        // getApplicationIdsResponse length: 2 data: 9100 = no applications on card
        // getApplicationIdsResponse length: 5 data: a1a2a3 9100
        // there might be more application on the card that fit into one frame:
        // getApplicationIdsResponse length: 5 data: a1a2a3 91AF
        // AF at the end is indicating more data

        // check that result if 0x9100 (success) or 0x91AF (success but more data)
        if ((!checkResponse(getApplicationIdsResponse)) && (!checkResponseMoreData(getApplicationIdsResponse))) {
            // something got wrong (e.g. missing authentication ?)
            writeToUiAppend(logTextView, "there was an unexpected response");
            return null;
        }
        // if the read result is success 9100 we return the data received so far
        if (checkResponse(getApplicationIdsResponse)) {
            System.arraycopy(returnStatusBytes(getApplicationIdsResponse), 0, response, 0, 2);
            byte[] applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
            applicationIdList = divideArray(applicationListBytes, 3);
            return applicationIdList;
        }
        if (checkResponseMoreData(getApplicationIdsResponse)) {
            writeToUiAppend(logTextView, "getApplicationIdsList: we are asked to grab more data from the card");
            byte[] applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
            applicationIdList = divideArray(applicationListBytes, 3);
            byte getMoreDataCommand = (byte) 0xaf;
            boolean readMoreData = true;
            try {
                while (readMoreData) {
                    try {
                        getApplicationIdsResponse = isoDep.transceive(wrapMessage(getMoreDataCommand, null));
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
                        return null;
                    }
                    writeToUiAppend(logTextView, printData("getApplicationIdsResponse", getApplicationIdsResponse));
                    if (checkResponse(getApplicationIdsResponse)) {
                        // now we have received all data
                        List<byte[]> applicationIdListTemp = new ArrayList<>();
                        System.arraycopy(returnStatusBytes(getApplicationIdsResponse), 0, response, 0, 2);
                        applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
                        applicationIdListTemp = divideArray(applicationListBytes, 3);
                        readMoreData = false; // end the loop
                        applicationIdList.addAll(applicationIdListTemp);
                        return applicationIdList;
                    }
                    if (checkResponseMoreData(getApplicationIdsResponse)) {
                        // some more data will follow, store temp data
                        List<byte[]> applicationIdListTemp = new ArrayList<>();
                        applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
                        applicationIdListTemp = divideArray(applicationListBytes, 3);
                        applicationIdList.addAll(applicationIdListTemp);
                        readMoreData = true;
                    }
                } // while (readMoreData) {
            } catch (Exception e) {
                writeToUiAppend(logTextView, "Exception failure: " + e.getMessage());
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
            } // try
        }
        return null;
    }

    private boolean createApplicationPlainDes(TextView logTextView, byte[] applicationIdentifier, byte numberOfKeys, byte[] response) {
        if (logTextView == null) return false;
        if (applicationIdentifier == null) return false;
        if (applicationIdentifier.length != 3) return false;

        // create an application
        writeToUiAppend(logTextView, "create the application " + Utils.bytesToHex(applicationIdentifier));
        byte createApplicationCommand = (byte) 0xca;
        byte applicationMasterKeySettings = (byte) 0x0f;
        byte[] createApplicationParameters = new byte[5];
        System.arraycopy(applicationIdentifier, 0, createApplicationParameters, 0, applicationIdentifier.length);
        createApplicationParameters[3] = applicationMasterKeySettings;
        createApplicationParameters[4] = numberOfKeys;
        writeToUiAppend(logTextView, printData("createApplicationParameters", createApplicationParameters));
        byte[] createApplicationResponse = new byte[0];
        try {
            createApplicationResponse = isoDep.transceive(wrapMessage(createApplicationCommand, createApplicationParameters));
            writeToUiAppend(logTextView, printData("createApplicationResponse", createApplicationResponse));
            System.arraycopy(returnStatusBytes(createApplicationResponse), 0, response, 0, 2);
            //System.arraycopy(createApplicationResponse, 0, response, 0, createApplicationResponse.length);
            if (checkResponse(createApplicationResponse)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "createApplicationAes transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
    }

    private boolean selectApplicationDes(TextView logTextView, byte[] applicationIdentifier, byte[] response) {
        // select application
        byte selectApplicationCommand = (byte) 0x5a;
        byte[] selectApplicationResponse = new byte[0];
        try {
            selectApplicationResponse = isoDep.transceive(wrapMessage(selectApplicationCommand, applicationIdentifier));
            writeToUiAppend(logTextView, printData("selectApplicationResponse", selectApplicationResponse));
            System.arraycopy(returnStatusBytes(selectApplicationResponse), 0, response, 0, 2);
            //System.arraycopy(selectApplicationResponse, 0, response, 0, selectApplicationResponse.length);
            if (checkResponse(selectApplicationResponse)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "selectApplicationDes transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
    }

    /**
     * section for key handling
     */

    private boolean changeKeyDes(TextView logTextView, byte keyNumber, byte[] oldKey, byte[] newKey, byte[] response) {
        // sanity checks
        if (keyNumber > (byte) 0x0f) return false; // todo this check is incomplete, use maximum key number from key settings
        if ((oldKey == null) | (newKey == null)) return false;
        if (oldKey.length != 8) return false;
        if (newKey.length != 8) return false;
        if (SESSION_KEY_TDES == null) return false;

        System.out.println("ChangeKeyDes keyNumber: " + Utils.byteToHex(keyNumber) + " is authenticated with keyNr " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
        System.out.println(printData("oldKey", oldKey));
        System.out.println(printData("newKey", newKey));

        // this method is using a fixed key version
        byte KEY_VERSION = 0;

        setKeyVersion(oldKey, 0, oldKey.length, KEY_VERSION);

        byte[] plaintext = new byte[24]; // this is the final array
        int nklen = 16;
        System.out.println(printData("newKey before setKeyVersion", newKey));
        setKeyVersion(newKey, 0, newKey.length, KEY_VERSION);
        System.arraycopy(newKey, 0, plaintext, 0, newKey.length);
        System.out.println(printData("newKey after", newKey));
        System.out.println(printData("plaintext", plaintext));
        // 8-byte DES keys accepted: internally have to be handled w/ 16 bytes
        System.arraycopy(newKey, 0, plaintext, 8, newKey.length);
        newKey = Arrays.copyOfRange(plaintext, 0, 16);
        System.out.println(printData("newKey TDES", newKey));

        // xor the new key with the old key if a key is changed different to authentication
        if ((keyNumber & 0x0F) != KEY_NUMBER_USED_FOR_AUTHENTICATION) {
            for (int i = 0; i < newKey.length; i++) {
                plaintext[i] ^= oldKey[i % oldKey.length];
            }
        }
        System.out.println(printData("plaintext", plaintext));

        byte[] crc;
        int addDesKeyVersionByte = (byte) 0x00;

        crc = CRC16.get(plaintext, 0, nklen + addDesKeyVersionByte);
        System.arraycopy(crc, 0, plaintext, nklen + addDesKeyVersionByte, 2);

        if ((keyNumber & 0x0F) != KEY_NUMBER_USED_FOR_AUTHENTICATION) {
            crc = CRC16.get(newKey);
            System.arraycopy(crc, 0, plaintext, nklen + addDesKeyVersionByte + 2, 2);
        }
        System.out.println(printData("plaintext before encryption", plaintext));
        byte[] ciphertext = null;
        System.out.println(printData("SESSION_KEY_DES", SESSION_KEY_TDES));
        ciphertext = decrypt(SESSION_KEY_TDES, plaintext);
        System.out.println(printData("ciphertext after encryption", ciphertext));

        byte changeKeyCommand = (byte) 0xc4;
        byte[] apdu = new byte[5 + 1 + ciphertext.length + 1];
        apdu[0] = (byte) 0x90;
        apdu[1] = changeKeyCommand;
        apdu[4] = (byte) (1 + plaintext.length);
        apdu[5] = keyNumber;
        System.arraycopy(ciphertext, 0, apdu, 6, ciphertext.length);
        System.out.println(printData("apdu", apdu));

        byte[] changeKeyDesResponse = new byte[0];
        try {
            //response = isoDep.transceive(wrapMessage(selectApplicationCommand, applicationIdentifier));
            changeKeyDesResponse = isoDep.transceive(apdu);
            writeToUiAppend(logTextView, printData("changeKeyDesResponse", changeKeyDesResponse));
            System.arraycopy(returnStatusBytes(changeKeyDesResponse), 0, response, 0, 2);
            //System.arraycopy(selectApplicationResponse, 0, response, 0, selectApplicationResponse.length);
            if (checkResponse(changeKeyDesResponse)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "changeKeyDes transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
    }

    /**
     * Set the version on a DES key. Each least significant bit of each byte of
     * the DES key, takes one bit of the version. Since the version is only
     * one byte, the information is repeated if dealing with 16/24-byte keys.
     *
     * @param a			1K/2K/3K 3DES
     * @param offset	start position of the key within a
     * @param length	key length
     * @param version	the 1-byte version
     * Source: DESFireEV1.java (NFCJLIB)
     */
    private static void setKeyVersion(byte[] a, int offset, int length, byte version) {
        if (length == 8 || length == 16 || length == 24) {
            for (int i = offset + length - 1, j = 0; i >= offset; i--, j = (j + 1) % 8) {
                a[i] &= 0xFE;
                a[i] |= ((version >>> j) & 0x01);
            }
        }
    }

    // DES/3DES decryption: CBC send mode and CBC receive mode
    // here fixed to SEND_MODE = decrypt
    private static byte[] decrypt(byte[] key, byte[] data) {

        /* this method
        plaintext before encryption length: 24 data: d400000000000000d4000000000000007f917f9100000000
        ciphertext after encryption length: 24 data: 3b93de449348de6a16c92664a51d152d5d07194befeaa71d
         */
        /* method from DESFireEV1.java
        plaintext before encryption: d400000000000000d4000000000000007f917f9100000000
        ciphertext after encryption: 2c1ba72be0074ee529f8b450bfe42a465196116967b8272f
         */


        byte[] modifiedKey = new byte[24];
        System.arraycopy(key, 0, modifiedKey, 16, 8);
        System.arraycopy(key, 0, modifiedKey, 8, 8);
        System.arraycopy(key, 0, modifiedKey, 0, key.length);

        /* MF3ICD40, which only supports DES/3DES, has two cryptographic
         * modes of operation (CBC): send mode and receive mode. In send mode,
         * data is first XORed with the IV and then decrypted. In receive
         * mode, data is first decrypted and then XORed with the IV. The PCD
         * always decrypts. The initial IV, reset in all operations, is all zeros
         * and the subsequent IVs are the last decrypted/plain block according with mode.
         *
         * MDF EV1 supports 3K3DES/AES and remains compatible with MF3ICD40.
         */
        byte[] ciphertext = new byte[data.length];
        byte[] cipheredBlock = new byte[8];

                // XOR w/ previous ciphered block --> decrypt
                for (int i = 0; i < data.length; i += 8) {
                    for (int j = 0; j < 8; j++) {
                        data[i + j] ^= cipheredBlock[j];
                    }
                    cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
                    System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
                }
        return ciphertext;
    }

    /**
     * section for command and response handling
     */

    private byte[] wrapMessage(byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null) {
            stream.write((byte) parameters.length);
            stream.write(parameters);
        }
        stream.write((byte) 0x00);
        return stream.toByteArray();
    }

    private byte[] returnStatusBytes(byte[] data) {
        return Arrays.copyOfRange(data, (data.length - 2), data.length);
    }

    /**
     * checks if the response has an 0x'9100' at the end means success
     * and the method returns the data without 0x'9100' at the end
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return
     */
    private boolean checkResponse(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status == 0x9100) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if the response has an 0x'91AF' at the end means success
     * but there are more data frames available
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return
     */
    private boolean checkResponseMoreData(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status == 0x91AF) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if the response has an 0x'91de' at the end means the data
     * element is already existing
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return true is code is 91DE
     */
    private boolean checkDuplicateError(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x91DE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * splits a byte array in chunks
     *
     * @param source
     * @param chunksize
     * @return a List<byte[]> with sets of chunksize
     */
    private static List<byte[]> divideArray(byte[] source, int chunksize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }

    /**
     * splits a byte array in chunks of bytes
     *
     * @param source
     * @return a List<Byte>
     */
    private static List<Byte> divideArrayToBytes(byte[] source) {
        List<Byte> result = new ArrayList<Byte>();
        for (int i = 0; i < source.length; i++) {
           result.add(source[i]);
        }
        return result;
    }

    /**
     * section for NFC handling
     */

    // This method is run in another thread when a card is discovered
    // !!!! This method cannot cannot direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    @Override
    public void onTagDiscovered(Tag tag) {

        clearOutputFields();
        invalidateAllSelections();
        writeToUiAppend(output, "NFC tag discovered");
        isoDep = null;
        try {
            isoDep = IsoDep.get(tag);
            if (isoDep != null) {
                /*
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "NFC tag is IsoDep compatible",
                            Toast.LENGTH_SHORT).show();
                });
                 */

                // Make a Sound
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
                } else {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);
                }

                runOnUiThread(() -> {
                    output.setText("");
                    //output.setBackgroundColor(getResources().getColor(R.color.white));
                });
                isoDep.connect();
                if (!isoDep.isConnected()) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "could not connect to the tag, aborted", COLOR_RED);
                    isoDep.close();
                    return;
                }

                // setup the communication adapter
                adapter = new CommunicationAdapter(isoDep, true);

                // get tag ID
                tagIdByte = tag.getId();
                writeToUiAppend(output, "tag id: " + Utils.bytesToHex(tagIdByte));
                writeToUiAppend(output, "NFC tag connected");

            }

        } catch (IOException e) {
            writeToUiAppend(output, "ERROR: IOException " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            writeToUiAppend(output, "ERROR: Exception " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }

    }



    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for layout handling
     */
    private void allLayoutsInvisible() {
        llApplicationHandling.setVisibility(View.GONE);
        llStandardFile.setVisibility(View.GONE);
    }

    /**
     * section for UI handling
     */

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    private void writeToUiAppendBorderColor(TextView textView, TextInputLayout textInputLayout, String message, int color) {
        runOnUiThread(() -> {

            // set the color to green
            //Color from rgb
            // int color = Color.rgb(255,0,0); // red
            //int color = Color.rgb(0,255,0); // green
            //Color from hex string
            //int color2 = Color.parseColor("#FF11AA"); light blue
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_focused}, // focused
                    new int[] { android.R.attr.state_hovered}, // hovered
                    new int[] { android.R.attr.state_enabled}, // enabled
                    new int[] { }  //
            };
            int[] colors = new int[] {
                    color,
                    color,
                    color,
                    //color2
                    color
            };
            ColorStateList myColorList = new ColorStateList(states, colors);
            textInputLayout.setBoxStrokeColorStateList(myColorList);

            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    public String printData(String dataName, byte[] data) {
        int dataLength;
        String dataString = "";
        if (data == null) {
            dataLength = 0;
            dataString = "IS NULL";
        } else {
            dataLength = data.length;
            dataString = Utils.bytesToHex(data);
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append(dataName)
                .append(" length: ")
                .append(dataLength)
                .append(" data: ")
                .append(dataString);
        return sb.toString();
    }

    private void clearOutputFields() {
        runOnUiThread(() -> {
                    output.setText("");
                    errorCode.setText("");
                });
        // reset the border color to primary for errorCode
        int color = R.color.colorPrimary;
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_focused}, // focused
                new int[] { android.R.attr.state_hovered}, // hovered
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] { }  //
        };
        int[] colors = new int[] {
                color,
                color,
                color,
                color
        };
        ColorStateList myColorList = new ColorStateList(states, colors);
        errorCodeLayout.setBoxStrokeColorStateList(myColorList);
    }

    private void invalidateAllSelections() {
        selectedApplicationId = null;
        selectedFileId = "";
        runOnUiThread(() -> {
                    applicationSelected.setText("");
                    fileSelected.setText("");
                });
        KEY_NUMBER_USED_FOR_AUTHENTICATION = -1;
    }

    /**
     * section for options menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mApplications = menu.findItem(R.id.action_applications);
        mApplications.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llApplicationHandling.setVisibility(View.VISIBLE);
                return false;
            }
        });

        MenuItem mStandardFile = menu.findItem(R.id.action_standard_file);
        mStandardFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llStandardFile.setVisibility(View.VISIBLE);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}