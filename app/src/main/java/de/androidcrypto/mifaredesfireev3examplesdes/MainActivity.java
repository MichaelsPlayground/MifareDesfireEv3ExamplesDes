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
import android.widget.RadioButton;
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
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private static final String TAG = MainActivity.class.getName();

    private com.google.android.material.textfield.TextInputEditText output, errorCode;
    private com.google.android.material.textfield.TextInputLayout errorCodeLayout;

    /**
     * section for general workflow
     */

    private LinearLayout llGeneralWorkflow;
    private Button tagVersion, keySettings, freeMemory, formatPicc, selectMasterApplication;

    /**
     * section for application handling
     */
    private LinearLayout llApplicationHandling;
    private Button applicationList, applicationCreate, applicationSelect;
    private com.google.android.material.textfield.TextInputEditText numberOfKeys, applicationId, applicationSelected;
    private byte[] selectedApplicationId = null;

    /**
     * section for files
     */

    private Button fileList, fileSelect, fileSettings, changeFileSettings;
    private com.google.android.material.textfield.TextInputEditText fileSelected;

    private String selectedFileId = "";
    private int selectedFileSize;
    private FileSettings selectedFileSettings;

    /**
     * section for standard file handling
     */

    private LinearLayout llStandardFile;
    private Button fileStandardCreate, fileStandardWrite, fileStandardRead;
    private com.shawnlin.numberpicker.NumberPicker npStandardFileId;
    private com.google.android.material.textfield.TextInputEditText fileStandardSize, fileStandardData;

    /**
     * section for value file handling
     */

    private LinearLayout llValueFile;
    private Button fileValueCreate, fileValueCredit, fileValueDebit, fileValueRead;
    private com.shawnlin.numberpicker.NumberPicker npValueFileId;
    private com.google.android.material.textfield.TextInputEditText lowerLimitValue, upperLimitValue, initialValueValue, creditDebitValue;

    /**
     * section for record files
     */

    private LinearLayout llRecordFile;
    private Button fileRecordCreate, fileRecordWrite, fileRecordRead;
    private RadioButton rbLinearRecordFile, rbCyclicRecordFile;
    private com.shawnlin.numberpicker.NumberPicker npRecordFileId;
    private com.google.android.material.textfield.TextInputEditText fileRecordSize, fileRecordData, fileRecordNumberOfRecords;

    /**
     * work with encrypted standard files - EXPERIMENTAL
     */

    private LinearLayout llStandardFileEnc;
    private Button fileStandardCreateEnc, fileStandardWriteEnc;

    /**
     * section for authentication
     */

    private Button authKeyD0, authKeyD1, authKeyD2, authKeyD3, authKeyD4;
    private Button authKeyD0C;

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
    private final byte[] DES_KEY_D0 = Utils.hexStringToByteArray("D000000000000000"); // key number 0 is the application master key
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

    int COLOR_GREEN = Color.rgb(0, 255, 0);
    int COLOR_RED = Color.rgb(255, 0, 0);

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
        keySettings = findViewById(R.id.btnGetKeySettings);
        freeMemory = findViewById(R.id.btnGetFreeMemory);
        formatPicc = findViewById(R.id.btnFormatPicc);
        selectMasterApplication = findViewById(R.id.btnSelectMasterApplication);

        // application handling
        llApplicationHandling = findViewById(R.id.llApplications);
        applicationList = findViewById(R.id.btnListApplications);
        applicationCreate = findViewById(R.id.btnCreateApplication);
        applicationSelect = findViewById(R.id.btnSelectApplication);
        applicationSelected = findViewById(R.id.etSelectedApplicationId);
        numberOfKeys = findViewById(R.id.etNumberOfKeys);
        applicationId = findViewById(R.id.etApplicationId);

        // file handling
        fileList = findViewById(R.id.btnListFiles);
        fileSelect = findViewById(R.id.btnSelectFile);
        fileSettings = findViewById(R.id.btnGetFileSettings);
        changeFileSettings = findViewById(R.id.btnChangeFileSettings);
        fileSelected = findViewById(R.id.etSelectedFileId);

        // standard file handling
        llStandardFile = findViewById(R.id.llStandardFile);
        fileStandardCreate = findViewById(R.id.btnCreateStandardFile);
        fileStandardWrite = findViewById(R.id.btnWriteStandardFile);
        fileStandardRead = findViewById(R.id.btnReadStandardFile);
        npStandardFileId = findViewById(R.id.npStandardFileId);
        fileStandardSize = findViewById(R.id.etFileStandardSize);
        fileStandardData = findViewById(R.id.etFileStandardData);

        // value file handling
        llValueFile = findViewById(R.id.llValueFile);
        fileValueCreate = findViewById(R.id.btnCreateValueFile);
        fileValueRead = findViewById(R.id.btnReadValueFile);
        fileValueCredit = findViewById(R.id.btnCreditValueFile);
        fileValueDebit = findViewById(R.id.btnDebitValueFile);
        npValueFileId = findViewById(R.id.npValueFileId);
        lowerLimitValue = findViewById(R.id.etValueLowerLimit);
        upperLimitValue = findViewById(R.id.etValueUpperLimit);
        initialValueValue = findViewById(R.id.etValueInitialValue);
        creditDebitValue = findViewById(R.id.etValueCreditDebitValue);

        llRecordFile = findViewById(R.id.llRecordFile);
        fileRecordCreate = findViewById(R.id.btnCreateRecordFile);
        fileRecordRead = findViewById(R.id.btnReadRecordFile);
        fileRecordWrite = findViewById(R.id.btnWriteRecordFile);
        npRecordFileId = findViewById(R.id.npRecordFileId);
        fileRecordSize = findViewById(R.id.etRecordFileSize);
        fileRecordNumberOfRecords = findViewById(R.id.etRecordFileNumberRecords);
        fileRecordData = findViewById(R.id.etRecordFileData);
        rbLinearRecordFile = findViewById(R.id.rbLinearRecordFile);
        rbCyclicRecordFile = findViewById(R.id.rbCyclicRecordFile);

        // encrypted standard file handling
        llStandardFileEnc = findViewById(R.id.llStandardFileEnc);
        fileStandardCreateEnc = findViewById(R.id.btnCreateStandardFileEnc);
        fileStandardWriteEnc = findViewById(R.id.btnWriteStandardFileEnc);

        // authentication handling
        authKeyD0 = findViewById(R.id.btnAuthD0);
        authKeyD0C = findViewById(R.id.btnAuthD0C);
        authKeyD1 = findViewById(R.id.btnAuthD1);
        authKeyD2 = findViewById(R.id.btnAuthD2);
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

        freeMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the free memory on the tag
                clearOutputFields();
                writeToUiAppend(output, "get the free memory on the card");
                byte[] responseData = new byte[2];
                int result = getFreeMemory(output, responseData);
                writeToUiAppend(output, "getFreeMemory: " + result);
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFreeMemory: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        formatPicc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the free memory on the tag
                clearOutputFields();
                writeToUiAppend(output, "format the PICC");

                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                boolean success;
                                byte[] responseData = new byte[2];
                                success = formatPicc(output, responseData);
                                writeToUiAppend(output, "formatPiccSuccess: " + success);
                                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFreeMemory: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "format of the PICC aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to format the PICC " + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("FORMAT the PICC")
                        .show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */
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
                writeToUiAppend(output, "create an application with id: " + Utils.bytesToHexNpe(applicationIdentifier));
                // change the aid to LSB
                Utils.reverseByteArrayInPlace(applicationIdentifier);
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
                    byte[] aid = applicationIdList.get(i);
                    Utils.reverseByteArrayInPlace(aid);
                    applicationList[i] = Utils.bytesToHexNpe(aid);
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
                        byte[] aid = selectedApplicationId.clone();
                        Utils.reverseByteArrayInPlace(aid);
                        boolean result = selectApplicationDes(output, aid, responseData);
                        writeToUiAppend(output, "result of selectApplicationDes: " + result);
                        //writeToUiAppend(errorCode, "selectApplicationDes: " + Ev3.getErrorCode(responseData));
                        int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "selectApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
                        applicationSelected.setText(applicationList[which]);
                        invalidateEncryptionKeys();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        /**
         * section  for files
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
                        if ((fileSettingsBytes != null) && (fileSettingsBytes.length >= 7)) {
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
                // check that an application is already selected
                if (selectedApplicationId == null) {
                    writeToUiAppend(output, "you need to select an application first, aborted");
                    return;
                }

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
                    // get the file type for each entry
                    byte fileId = fileIdList.get(i);
                    FileSettings fileSettings;
                    String fileTypeName = "unknown";
                    byte[] fileSettingsBytes = getFileSettings(output, fileId, responseData);
                    if ((fileSettingsBytes != null) && (fileSettingsBytes.length >= 7)) {
                        fileSettings = new FileSettings(fileId, fileSettingsBytes);
                        fileTypeName = fileSettings.getFileTypeName();
                    }
                    //fileList[i] = Utils.byteToHex(fileIdList.get(i)) + " (" + fileTypeName + ")";
                    fileList[i] = fileIdList.get(i) + " (" + fileTypeName + ")";
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a file");

                builder.setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you  selected nr " + which + " = " + fileList[which]);
                        //selectedFileId = fileList[which];
                        selectedFileId = fileIdList.get(which).toString();
                        // now we run the command to select the application
                        byte[] responseData = new byte[2];
                        //boolean result = selectDes(output, selectedApplicationId, responseData);
                        //writeToUiAppend(output, "result of selectApplicationDes: " + result);
                        //writeToUiAppend(errorCode, "selectApplicationDes: " + Ev3.getErrorCode(responseData));

                        // here we are reading the fileSettings
                        String outputString = fileList[which] + " ";
                        byte fileIdByte = Byte.parseByte(selectedFileId);
                        byte[] fileSettingsBytes = getFileSettings(output, fileIdByte, responseData);
                        if ((fileSettingsBytes != null) && (fileSettingsBytes.length >= 7)) {
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

        fileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearOutputFields();
                writeToUiAppend(output, "show the file settings of the selected file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "file settings of file " + selectedFileSettings.getFileNumberInt() + "\n" + selectedFileSettings.dump());
            }
        });

        changeFileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearOutputFields();
                writeToUiAppend(output, "change the file settings of the selected file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                boolean changeResult = changeTheFileSettings();
                writeToUiAppend(output, "the changeFileSettings was " + changeResult);
                //writeToUiAppend(output, "file settings of file " + selectedFileSettings.getFileNumberInt() + "\n" + selectedFileSettings.dump());
            }
        });

        /**
         * section for standard files
         */

        fileStandardCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new standard file
                // get the input and sanity checks
                clearOutputFields();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a limit of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                // this uses the numberPicker
                byte fileIdByte = (byte) (npStandardFileId.getValue() & 0xFF);
                // this is done with an EditText
                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileStandardSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    //writeToUiAppend(errorCode, "you entered a wrong file ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                if (fileSizeInt < 1) {
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
                String dataToWrite = fileStandardData.getText().toString();
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
                // read from a preselected file
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                byte fileIdByte = Byte.parseByte(selectedFileId);
                // check that it is a standard file
                if (!selectedFileSettings.getFileTypeName().equals(FileSettings.STANDARD_FILE_TYPE)) {
                    writeToUiAppend(output, "the selected fileID: " + fileIdByte + " is not of type Standard but of type "
                            + selectedFileSettings.getFileTypeName() + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromStandardFile - wrong selected fileID", COLOR_RED);
                    return;
                }
                byte[] responseData = new byte[2];
                byte[] result = readFromStandardFile(output, fileIdByte, responseData);
                //byte[] result = readFromStandardFileLimitedSize(output, fileIdByte, responseData);
                writeToUiAppend(output, "readFromStandardFile" + " ID: " + fileIdByte + printData(" data", result));
                writeToUiAppend(output, "readFromStandardFile" + " ID: " + fileIdByte + " data: " + new String(result, StandardCharsets.UTF_8));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        /**
         * section for value files
         */

        fileValueCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new value file
                // get the input and sanity checks
                clearOutputFields();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a limit of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                // this uses the numberPicker
                byte fileIdByte = (byte) (npValueFileId.getValue() & 0xFF);
                // this is done with an EditText
                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());

                if (fileIdByte > (byte) 0x0f) {
                    //writeToUiAppend(errorCode, "you entered a wrong file ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }

                int lowerLimitInt = Integer.parseInt(lowerLimitValue.getText().toString());
                int upperLimitInt = Integer.parseInt(upperLimitValue.getText().toString());
                int initialValueInt = Integer.parseInt(initialValueValue.getText().toString());

                PayloadBuilder pb = new PayloadBuilder();

                if ((lowerLimitInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (lowerLimitInt > pb.getMAXIMUM_VALUE_LOWER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong lower limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if ((upperLimitInt < pb.getMINIMUM_VALUE_UPPER_LIMIT()) || (upperLimitInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if (upperLimitInt <= lowerLimitInt) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, should be higher than lower limit", COLOR_RED);
                    return;
                }
                if ((initialValueInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (initialValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong initial value, should be between lower and higher limit", COLOR_RED);
                    return;
                }

                try {
                    byte[] createValueFileParameters = pb.createValueFile(fileIdByte, PayloadBuilder.CommunicationSetting.Plain,
                            1, 2, 3, 4, lowerLimitInt, upperLimitInt, initialValueInt, false);
                    writeToUiAppend(output, printData("createValueFileParameters", createValueFileParameters));
                    byte createValueFileCommand = (byte) 0xcc;
                    byte[] apdu = wrapMessage(createValueFileCommand, createValueFileParameters);
                    byte[] response = adapter.sendSimple(apdu);
                    if (checkDuplicateError(response)) {
                        writeToUiAppend(output, "the file was not created as it already exists, proceed");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAValueFile - the fileID is existing: " + Ev3.getErrorCode(response), COLOR_GREEN);
                        return;
                    }
                    if (checkResponse(response)) {
                        writeToUiAppend(output, "createValueFile " + " with FileID: " + Utils.byteToHex(fileIdByte)
                                + " lower limit: " + lowerLimitInt + " upper limit: " + upperLimitInt + " initial limit: " + initialValueInt + " SUCCESS");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAValueFile SUCCESS", COLOR_GREEN);
                        return;
                    } else {
                        int colorFromErrorCode = Ev3.getColorFromErrorCode(response);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAValueFile FAILURE: " + Ev3.getErrorCode(response), colorFromErrorCode);
                        writeToUiAppend(output, "createValueFile " + checkResponse(response));
                    }
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileValueRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read from a preselected file
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);
                // check that it is a value file
                if (!selectedFileSettings.getFileTypeName().equals(FileSettings.VALUE_FILE_TYPE)) {
                    writeToUiAppend(output, "the selected fileID: " + fileIdInt + " is not of type Value but of type "
                            + selectedFileSettings.getFileTypeName() + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromValueFile - wrong selected fileID", COLOR_RED);
                    return;
                }
                byte[] responseData = new byte[2];
                int result = readFromValueFile(output, fileIdByte, responseData);
                //byte[] result = readFromStandardFileLimitedSize(output, fileIdByte, responseData);
                writeToUiAppend(output, "readFromValueFile" + " ID: " + fileIdByte + " value: " + result + printData(" response", responseData));

                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromValueFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        fileValueCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // credit a value file
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);
                // check that it is a value file
                if (!selectedFileSettings.getFileTypeName().equals(FileSettings.VALUE_FILE_TYPE)) {
                    writeToUiAppend(output, "the selected fileID: " + fileIdInt + " is not of type Value but of type "
                            + selectedFileSettings.getFileTypeName() + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromValueFile - wrong selected fileID", COLOR_RED);
                    return;
                }

                int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                if ((changeValueInt < 1) || (changeValueInt > PayloadBuilder.MAXIMUM_VALUE_CREDIT)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                    return;
                }

                byte[] responseData = new byte[2];
                boolean result = creditValueFile(output, fileIdInt, changeValueInt, responseData);
                //byte[] result = readFromStandardFileLimitedSize(output, fileIdByte, responseData);
                writeToUiAppend(output, "creditValueFile" + " ID: " + fileIdByte + " credit value: " + changeValueInt + " result: " + result);

                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "creditValueFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

                // dont forget to commit the change
                boolean commitSuccess = commitWriteToFile(output, responseData);
                writeToUiAppend(output, "result of commit: " + commitSuccess);
                //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        fileValueDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // credit a value file
                clearOutputFields();
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);
                // check that it is a value file
                if (!selectedFileSettings.getFileTypeName().equals(FileSettings.VALUE_FILE_TYPE)) {
                    writeToUiAppend(output, "the selected fileID: " + fileIdInt + " is not of type Value but of type "
                            + selectedFileSettings.getFileTypeName() + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readFromValueFile - wrong selected fileID", COLOR_RED);
                    return;
                }

                int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                if ((changeValueInt < 1) || (changeValueInt > PayloadBuilder.MAXIMUM_VALUE_CREDIT)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                    return;
                }

                byte[] responseData = new byte[2];
                boolean result = debitValueFile(output, fileIdInt, changeValueInt, responseData);
                //byte[] result = readFromStandardFileLimitedSize(output, fileIdByte, responseData);
                writeToUiAppend(output, "debitValueFile" + " ID: " + fileIdByte + " debit value: " + changeValueInt + " result: " + result);

                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "debitValueFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

                // dont forget to commit the change
                boolean commitSuccess = commitWriteToFile(output, responseData);
                writeToUiAppend(output, "result of commit: " + commitSuccess);
                //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        /**
         * section for record files
         */

        fileRecordCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new value file
                // get the input and sanity checks
                clearOutputFields();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a limit of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                // this uses the numberPicker
                byte fileIdByte = (byte) (npRecordFileId.getValue() & 0xFF);
                // this is done with an EditText
                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    //writeToUiAppend(errorCode, "you entered a wrong file ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                int fileSizeInt = Integer.parseInt(fileRecordSize.getText().toString());
                if (fileSizeInt < 1) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file size, 32 bytes allowed only", COLOR_RED);
                    return;
                }

                int fileNumberOfRecordsInt = Integer.parseInt(fileRecordNumberOfRecords.getText().toString());
                if (fileNumberOfRecordsInt < 2) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 record number (minimum 2)", COLOR_RED);
                    return;
                }

                // get the type of file - linear or cyclic
                boolean isLinearRecordFile = rbLinearRecordFile.isChecked();
                boolean isCyclicRecordFile = rbCyclicRecordFile.isChecked();

                String fileTypeString = "";
                if (isLinearRecordFile) {
                    fileTypeString = "LinearRecord";
                } else {
                    fileTypeString = "CyclicRecord";
                }

                byte[] responseData = new byte[2];
                boolean result = createRecordFile(output, fileIdByte, fileSizeInt, fileNumberOfRecordsInt, isLinearRecordFile, responseData);
                writeToUiAppend(output, "result of createARecordFile: " + result + " ID: " + fileIdByte + " size: " + fileSizeInt + " nbr of records: " + fileNumberOfRecordsInt);
                //writeToUiAppend(errorCode, "createAStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create" + fileTypeString + "File Success: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

            }
        });

        fileRecordWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected record file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to a record file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileRecordData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileSettings.getFileNumberInt();
                try {
                    // check that it is a record file !
                    // get the maximal length from getFileSettings
                    // check that it is a record file !
                    String fileTypeName = selectedFileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    boolean isLinearRecordFile = false;
                    if (fileTypeName.equals(FileSettings.LINEAR_RECORD_FILE_TYPE)) {
                        isLinearRecordFile = true;
                        writeToUiAppend(output, "The selected file is of type Linear Record File");
                    } else if (fileTypeName.equals(FileSettings.CYCLIC_RECORD_FILE_TYPE)) {
                        isLinearRecordFile = false;
                        writeToUiAppend(output, "The selected file is of type Cyclic Record File");
                    } else {
                        writeToUiAppend(output, "The selected file is not of type Linear or Cyclic Record but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }

                    // update the file settings data as they could have changed since last reading
                    byte[] responseDataFileSettings = new byte[2];
                    byte[] fileSettingsBytes = getFileSettings(output, selectedFileSettings.getFileNumber(), responseDataFileSettings);
                    if ((fileSettingsBytes != null) && (fileSettingsBytes.length >= 7)) {
                        selectedFileSettings = new FileSettings(selectedFileSettings.getFileNumber(), fileSettingsBytes);
                    } else {
                        // some error while retrieving the data
                        writeToUiAppend(output, "could not read the file settings of the selected file, aborted");
                        int colorFromErrorCode = Ev3.getColorFromErrorCode(responseDataFileSettings);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on reading the fileSettings: " + Ev3.getErrorCode(responseDataFileSettings), colorFromErrorCode);
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read Access Key first ?");
                        return;
                    }

                    int recordSize = selectedFileSettings.getRecordSizeInt();
                    int currentRecords = selectedFileSettings.getRecordsExistingInt();
                    int maxRecords = selectedFileSettings.getRecordsMaxInt();
                    writeToUiAppend(output, "recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);

                    // todo check maximum records for linear records file - if maximum is reached stop any further writing

                    // get a random payload with 32 bytes
                    UUID uuid = UUID.randomUUID(); // this is 36 characters long
                    //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long
                    byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);
                    byte[] fullDataToWrite = new byte[recordSize];
                    if (dataToWrite.length > recordSize) {
                        System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, recordSize);
                        writeToUiAppend(output, "your data is too long, shortened to length " + recordSize);
                    } else {
                        System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);
                    }
                    writeToUiAppend(output, printData("dataToWrite", fullDataToWrite));
                    //fullDataToWrite = Utils.generateTestData(recordSize); // create random testdata

                    byte[] responseData = new byte[2];
                    //boolean result = writeToStandardFile(output, fileIdByte, dataToWrite.getBytes(StandardCharsets.UTF_8), responseData);
                    boolean result = writeToRecordFile(output, selectedFileSettings.getFileNumber(), fullDataToWrite, isLinearRecordFile, responseData);
                    //writeToUiAppend(output, "result of writeToStandardFile: " + result + " ID: " + fileIdByte + " data: " + dataToWrite);
                    writeToUiAppend(output, "result of writeToRecordFile: " + result + " to fileID: " + selectedFileSettings.getFileNumber());
                    //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                    int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeToRecordFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

                    // dont forget to commit the change
                    boolean commitSuccess = commitWriteToFile(output, responseData);
                    writeToUiAppend(output, "result of commit: " + commitSuccess);
                    colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit: " + Ev3.getErrorCode(responseData), colorFromErrorCode);

                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileRecordRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                writeToUiAppend(output, "read from a record file");

                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }

                int fileIdInt = selectedFileSettings.getFileNumberInt();
                int recordSize;
                int currentRecords;
                try {
                    // check that it is a record file !
                    // get the maximal length from getFileSettings
                    // check that it is a record file !
                    String fileTypeName = selectedFileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    boolean isLinearRecordFile = false;
                    if (fileTypeName.equals(FileSettings.LINEAR_RECORD_FILE_TYPE)) {
                        isLinearRecordFile = true;
                        writeToUiAppend(output, "The selected file is of type Linear Record File");
                    } else if (fileTypeName.equals(FileSettings.CYCLIC_RECORD_FILE_TYPE)) {
                        isLinearRecordFile = false;
                        writeToUiAppend(output, "The selected file is of type Cyclic Record File");
                    } else {
                        writeToUiAppend(output, "The selected file is not of type Linear or Cyclic Record but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }

                    // update the file settings data as they could have changed since last reading
                    byte[] responseDataFileSettings = new byte[2];
                    byte[] fileSettingsBytes = getFileSettings(output, selectedFileSettings.getFileNumber(), responseDataFileSettings);
                    if ((fileSettingsBytes != null) && (fileSettingsBytes.length >= 7)) {
                        selectedFileSettings = new FileSettings(selectedFileSettings.getFileNumber(), fileSettingsBytes);
                    } else {
                        // some error while retrieving the data
                        writeToUiAppend(output, "could not read the file settings of the selected file, aborted");
                        int colorFromErrorCode = Ev3.getColorFromErrorCode(responseDataFileSettings);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on reading the fileSettings: " + Ev3.getErrorCode(responseDataFileSettings), colorFromErrorCode);
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read Access Key first ?");
                        return;
                    }

                    recordSize = selectedFileSettings.getRecordSizeInt();
                    currentRecords = selectedFileSettings.getRecordsExistingInt();
                    int maxRecords = selectedFileSettings.getRecordsMaxInt();
                    writeToUiAppend(output, "recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);

                    if (currentRecords == 0) {
                        writeToUiAppend(output, "there are no records to read (empty file ?)");
                        return;
                    }

                    byte[] readRecords; // will hold the complete data of all records
                    byte[] responseData = new byte[2];
                    //readRecords = desfire.readRecords((byte) (fileIdInt & 0xff), 0, 0);
                    int firstRecordToRead = 0; // reading all records, starting with the oldest = record 0
                    readRecords = readFromRecordFile(output, fileIdInt, firstRecordToRead, currentRecords, recordSize, responseData);
                    if ((readRecords == null) || (readRecords.length == 0)) {
                        writeToUiAppend(output, "there are no records to read (empty file ?)");
                        return;
                    }
                    List<byte[]> readRecordList = divideArray(readRecords, recordSize);
                    //readStandard = desfire.readData(STANDARD_FILE_NUMBER, 0, fileSize);
                    int listSize = readRecordList.size();
                    writeToUiAppend(output, "--------");
                    for (int i = 0; i < listSize; i++) {
                        byte[] record = readRecordList.get(i);
                        writeToUiAppend(output, "record " + i + printData(" data", record));
                        if (record != null) {
                            writeToUiAppend(output, new String(record, StandardCharsets.UTF_8));
                        }
                        writeToUiAppend(output, "--------");
                    }
                    writeToUiAppend(output, "finished");
                    writeToUiAppend(output, "");

                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a read access key ?");
                    e.printStackTrace();
                    return;
                }
            }
        });

        /**
         * section for encrypted standard files
         */

        fileStandardCreateEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new standard file
                // get the input and sanity checks
                clearOutputFields();
                writeToUiAppend(output, "create an encrypted standard file");
                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a limit of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                // this uses the numberPicker
                byte fileIdByte = (byte) (npStandardFileId.getValue() & 0xFF);
                // this is done with an EditText
                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileStandardSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    //writeToUiAppend(errorCode, "you entered a wrong file ID");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                if (fileSizeInt < 1) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file size, 32 bytes allowed only", COLOR_RED);
                    return;
                }
                byte[] responseData = new byte[2];
                boolean result = createStandardFileEncrypted(output, fileIdByte, fileSizeInt, responseData);
                writeToUiAppend(output, "result of createAStandardFile: " + result + " ID: " + fileIdByte + " size: " + fileSizeInt);
                //writeToUiAppend(errorCode, "createAStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createAStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            }
        });

        fileStandardWriteEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected standard file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to an encrypted standard file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWrite = fileStandardData.getText().toString();
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

                // check that a previous authentication has run so we do have a session key
                if ((SESSION_KEY_DES == null) || (KEY_NUMBER_USED_FOR_AUTHENTICATION < 0)) {
                    writeToUiAppend(output, "please run an authentication with a write access key first, aborted");
                    return;
                }

                byte fileIdByte = Byte.parseByte(selectedFileId);

                byte[] responseData = new byte[2];
                //boolean result = writeToStandardFile(output, fileIdByte, dataToWrite.getBytes(StandardCharsets.UTF_8), responseData);
                boolean result = writeToStandardFileEncrypted(output, fileIdByte, dataToWriteBytes, responseData);
                //writeToUiAppend(output, "result of writeToStandardFile: " + result + " ID: " + fileIdByte + " data: " + dataToWrite);
                writeToUiAppend(output, "result of writeToStandardFile: " + result + " to fileID: " + fileIdByte);
                //writeToUiAppend(errorCode, "writeToStandardFile: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeToStandardFile: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
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
                invalidateEncryptionKeys();
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

        authKeyD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 0 (Application Master Key) with DEFAULT KEY
                clearOutputFields();
                invalidateEncryptionKeys();
                try {
                    boolean authResult = authenticate(DES_KEY_D2_DEFAULT, DES_KEY_D2_NUMBER);
                    writeToUiAppend(output, "authResult: " + authResult);
                } catch (IOException e) {
                    writeToUiAppend(errorCode, "IOException " + e.getMessage());
                    throw new RuntimeException(e);
                }


                /*
                byte[] responseData = new byte[2];
                //byte keyId = (byte) 0x01; // we authenticate with keyId 0
                boolean result = authenticateApplicationDes(output, DES_KEY_D2_NUMBER, DES_KEY_D2_DEFAULT, true, responseData);
                writeToUiAppend(output, "result of authenticateApplicationDes: " + result);
                KEY_NUMBER_USED_FOR_AUTHENTICATION = DES_KEY_D2_NUMBER;
                writeToUiAppend(output, "key number: " + Utils.byteToHex(KEY_NUMBER_USED_FOR_AUTHENTICATION));
                writeToUiAppend(output, printData("SESSION_KEY_DES ", SESSION_KEY_DES));
                writeToUiAppend(output, printData("SESSION_KEY_TDES", SESSION_KEY_TDES));
                //writeToUiAppend(errorCode, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData));
                int colorFromErrorCode = Ev3.getColorFromErrorCode(responseData);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplicationDes: " + Ev3.getErrorCode(responseData), colorFromErrorCode);
            */
            }
        });

        authKeyD0C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authorization of keyNumber 0 (Application Master Key) with DEFAULT KEY
                clearOutputFields();
                invalidateEncryptionKeys();
                byte[] responseData = new byte[2];
                //byte keyId = (byte) 0x01; // we authenticate with keyId 0
                boolean result = authenticateApplicationDes(output, DES_KEY_D0_NUMBER, DES_KEY_D0, true, responseData);
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
                invalidateEncryptionKeys();
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
                invalidateEncryptionKeys();
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
                invalidateEncryptionKeys();
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
                invalidateEncryptionKeys();
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
                invalidateEncryptionKeys();
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
                invalidateEncryptionKeys();
            }
        });


    }

    /**
     * section for general workflow
     */

    public VersionInfo getVersionInfo(TextView logTextView) throws Exception {
        byte[] apdu = wrapMessage(GET_VERSION_INFO, null);
        return new VersionInfo(adapter.receiveResponseChain(adapter.sendRequestChain(apdu)));
        //byte[] bytes = sendRequest(logTextView, GET_VERSION_INFO);
        //return new VersionInfo(bytes);
    }

    private int getFreeMemory(TextView logTextView, byte[] response) {
        // get the free memory on the card
        byte getFreeMemoryCommand = (byte) 0x6e;
        byte[] getFreeMemoryResponse = new byte[0];
        byte[] apdu;
        try {
            apdu = wrapMessage(getFreeMemoryCommand, null);
            getFreeMemoryResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return 0;
        }
        writeToUiAppend(logTextView, printData("getFreeMemoryResponse", getFreeMemoryResponse));
        // getFreeMemoryResponse length: 5 data: 400800 9100 (EV1 2K after create 1 app + 1 32 byte file)
        // getFreeMemoryResponse length: 5 data: 000a00 9100 (EV2 2K empty)
        // getFreeMemoryResponse length: 5 data: 001400 9100 (EV2 4K empty)
        // 400800 = 00 08 40 = 2112 bytes
        // 000a00 = 00 0a 00 = 2560 bytes
        // 001400 = 00 14 00 = 5120 bytes
        int memorySize = 0;
        if (getFreeMemoryResponse.length > 2) {
            byte[] lengthBytes = Arrays.copyOf(getFreeMemoryResponse, getFreeMemoryResponse.length - 2);
            memorySize = Utils.intFrom3ByteArrayInversed(lengthBytes);
            writeToUiAppend(logTextView, "free memory on card: " + memorySize);
        }
        System.arraycopy(returnStatusBytes(getFreeMemoryResponse), 0, response, 0, 2);
        return memorySize;
    }

    private boolean formatPicc(TextView logTextView, byte[] response) {
        // now we are formatting the card
        byte formatPiccCommand = (byte) 0xfc;
        byte[] formatPiccResponse = new byte[0];
        byte[] apdu;
        try {
            apdu = wrapMessage(formatPiccCommand, null);
            formatPiccResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("formatPiccResponse", formatPiccResponse));
        System.arraycopy(returnStatusBytes(formatPiccResponse), 0, response, 0, 2);
        if (checkResponse(formatPiccResponse)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * section for authentication with DES
     */

    // if verbose = true all steps are printed out
    private boolean authenticateApplicationDes(TextView logTextView, byte keyId, byte[] key, boolean verbose, byte[] response) {
        try {
            Log.d(TAG, "authenticateApplicationDes for keyId " + keyId + " and key " + Utils.bytesToHex(key));
            writeToUiAppend(logTextView, "authenticateApplicationDes for keyId " + keyId + " and key " + Utils.bytesToHex(key));
            // do DES auth
            //String getChallengeCommand = "901a0000010000";
            //String getChallengeCommand = "9084000000"; // IsoGetChallenge
            byte[] apdu = wrapMessage((byte) 0x1a, new byte[]{(byte) (keyId & 0xFF)});
            byte[] getChallengeResponse = adapter.sendSimple(apdu);
            //byte[] getChallengeResponse = isoDep.transceive(wrapMessage((byte) 0x1a, new byte[]{(byte) (keyId & 0xFF)}));
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
            byte[] challengeAnswerResponse = adapter.sendSimple(challengeAnswerAPDU);
            //byte[] challengeAnswerResponse = isoDep.transceive(challengeAnswerAPDU);
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
                SESSION_KEY_DES = generateD40SessionKeyDes(rndA, rndB); // this is a 16 bytes long key, but for D40 encryption (DES) we need 8 bytes only
                SESSION_KEY_TDES = new byte[16];
                System.arraycopy(SESSION_KEY_DES, 0, SESSION_KEY_TDES, 0, 8);
                System.arraycopy(SESSION_KEY_DES, 0, SESSION_KEY_TDES, 8, 8);
                writeToUiAppend(logTextView, printData("DES sessionKey", SESSION_KEY_DES));
                // as it is a single DES cryptography I'm using the first part of the SESSION_KEY_TDES only
                //SESSION_KEY_DES = Arrays.copyOf(SESSION_KEY_TDES, 8);
                return true;
            } else {
                writeToUiAppend(logTextView, "Authentication failed");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
                //SESSION_KEY_TDES = null;
                SESSION_KEY_DES = null;
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

    private static byte[] generateD40SessionKeyDes(byte[] randA, byte[] randB) {
        // this IS NOT described in the manual !!!
        /*
        RndA = 0000000000000000, RndB = A1A2A3A4A5A6A7A8
        sessionKey = 00000000A1A2A3A400000000A1A2A3A4 (16 byte
         */
        byte[] skey = new byte[8];
        System.arraycopy(randA, 0, skey, 0, 4);
        System.arraycopy(randB, 0, skey, 4, 4);
        return skey;
    }

    // code taken from DESFireEV1.java

    /**
     * Mutual authentication between PCD and PICC.
     *
     * @param key   the secret key (8 bytes for DES, 16 bytes for 3DES/AES and
     *              24 bytes for 3K3DES)
     * @param keyNo the key number
     * @throws IOException NOTE: this code is for KeyType DES only
     * @return true for success
     */
    public boolean authenticate(byte[] key, byte keyNo) throws IOException {
        setKeyVersion(key, 0, key.length, (byte) 0x00);
        final byte[] iv0 = new byte[8];
        byte[] apdu;
        byte[] responseAPDU;

        // 1st message exchange
        apdu = new byte[7];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) (0x0A);
        apdu[4] = 0x01;
        apdu[5] = keyNo;
        //responseAPDU = transmit(apdu);
        responseAPDU = adapter.sendSimple(apdu);
        Log.d(TAG, "authenticate " + printData("responseAPDU", responseAPDU));
        // we did not test here for "AF" as response
        byte[] responseData = Arrays.copyOf(responseAPDU, responseAPDU.length - 2);
        // step 3
        byte[] randB = recv(key, responseData, iv0);
        if (randB == null)
            return false;
        byte[] randBr = rotateLeft(randB);
        byte[] randA = new byte[randB.length];

        //fillRandom(randA);
        randA = Ev3.getRndADes();

        // step 3: encryption
        byte[] plaintext = new byte[randA.length + randBr.length];
        System.arraycopy(randA, 0, plaintext, 0, randA.length);
        System.arraycopy(randBr, 0, plaintext, randA.length, randBr.length);
        byte[] iv1 = Arrays.copyOfRange(responseData,
                responseData.length - iv0.length, responseData.length);
        byte[] ciphertext = send(key, plaintext, iv1);
        if (ciphertext == null)
            return false;

        // 2nd message exchange
        apdu = new byte[5 + ciphertext.length + 1];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0xAF;
        apdu[4] = (byte) ciphertext.length;
        System.arraycopy(ciphertext, 0, apdu, 5, ciphertext.length);
        //responseAPDU = transmit(apdu);
        responseAPDU = adapter.sendSimple(apdu);
        Log.d(TAG, "authenticate " + printData("responseAPDU", responseAPDU));
        // we did not test here for "AF" as response
        responseData = Arrays.copyOf(responseAPDU, responseAPDU.length - 2);

        // step 5
        byte[] iv2 = Arrays.copyOfRange(ciphertext,
                ciphertext.length - iv0.length, ciphertext.length);
        byte[] randAr = recv(key, responseData, iv2);
        if (randAr == null)
            return false;
        byte[] randAr2 = rotateLeft(randA);
        for (int i = 0; i < randAr2.length; i++)
            if (randAr[i] != randAr2[i])
                return false;

        // step 6
        byte[] skey = generateSessionKey(randA, randB);
        Log.d(TAG, "The random A is " + printData("", randA));
        Log.d(TAG, "The random B is " + printData("", randB));
        Log.d(TAG, "The skey     is " + printData("", skey));
        SESSION_KEY_DES = skey.clone();
        writeToUiAppend(output, printData("DES sessionKey", SESSION_KEY_DES));
        Log.d(TAG, "IV: " + printData("iv0", iv0));
        writeToUiAppend(output, printData("IV", iv0));
        return true;
    }

    // Receiving data that needs decryption.
    // this is using DES as KeyType only
    private static byte[] recv(byte[] key, byte[] data, byte[] iv) {
        return decrypt(key, data, DESMode.RECEIVE_MODE);
    }

    // IV sent is the global one but it is better to be explicit about it: can be null for DES/3DES
    // if IV is null, then it is set to zeros
    // Sending data that needs encryption.
    // NOTE: KeyType DES only
    private static byte[] send(byte[] key, byte[] data, byte[] iv) {
        return decrypt(key, data, DESMode.SEND_MODE);
    }

    /**
     * DES/3DES mode of operation.
     */
    private enum DESMode {
        SEND_MODE,
        RECEIVE_MODE;
    }

    // DES/3DES decryption: CBC send mode and CBC receive mode
    private static byte[] decrypt(byte[] key, byte[] data, DESMode mode) {
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

        switch (mode) {
            case SEND_MODE:
                // XOR w/ previous ciphered block --> decrypt
                for (int i = 0; i < data.length; i += 8) {
                    for (int j = 0; j < 8; j++) {
                        data[i + j] ^= cipheredBlock[j];
                    }
                    cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
                    System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
                }
                break;
            case RECEIVE_MODE:
                // decrypt --> XOR w/ previous plaintext block
                cipheredBlock = TripleDES.decrypt(modifiedKey, data, 0, 8);
                // implicitly XORed w/ IV all zeros
                System.arraycopy(cipheredBlock, 0, ciphertext, 0, 8);
                for (int i = 8; i < data.length; i += 8) {
                    cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
                    for (int j = 0; j < 8; j++) {
                        cipheredBlock[j] ^= data[i + j - 8];
                    }
                    System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
                }
                break;
            default:
                Log.e(TAG, "Wrong way (decrypt)");
                return null;
        }
        return ciphertext;
    }

    private static byte[] decrypt(byte[] data, byte[] key, byte[] IV) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, IV);
        return cipher.doFinal(data);
    }

    private static Cipher getCipher(int mode, byte[] key, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "DES");
        IvParameterSpec algorithmParamSpec = new IvParameterSpec(IV);
        cipher.init(mode, keySpec, algorithmParamSpec);
        return cipher;
    }

    // rotate the array one byte to the left
    private static byte[] rotateLeft(byte[] a) {
        byte[] ret = new byte[a.length];

        System.arraycopy(a, 1, ret, 0, a.length - 1);
        ret[a.length - 1] = a[0];

        return ret;
    }

    /**
     * Generate the session key using the random A generated by the PICC and
     * the random B generated by the PCD.
     *
     * @param randA the random number A
     * @param randB the random number B
     * @return the session key
     * <p>
     * NOTE: this is using KeyType DES only
     */
    private static byte[] generateSessionKey(byte[] randA, byte[] randB) {
        byte[] skey = null;
        skey = new byte[8];
        System.arraycopy(randA, 0, skey, 0, 4);
        System.arraycopy(randB, 0, skey, 4, 4);
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
            writeToUiAppend(logTextView, printData("send APDU", apdu));

            //readStandardFileResponse = adapter.send(apdu);
            byte[] readStandardFileResponse1st = adapter.sendRequestChain(apdu);
            writeToUiAppend(logTextView, printData("readStandardFileResponse1st", readStandardFileResponse1st));
            if (readStandardFileResponse1st == null) {
                writeToUiAppend(logTextView, "the readStandardFile command failed, aborted");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
                return null;
            }
            readStandardFileResponse = adapter.receiveResponseChain(readStandardFileResponse1st);
            writeToUiAppend(logTextView, printData("readStandardFileResponse2nd", readStandardFileResponse));

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
        } else if (readStandardFileResponse.length > expectedResponse) {
            // more data is provided - truncated
            return Arrays.copyOf(readStandardFileResponse, expectedResponse);
        } else {
            // less data is provided - we return as much as possible
            return readStandardFileResponse;
        }
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

    private boolean changeTheFileSettings() {
        int selectedFileIdInt = Integer.parseInt(selectedFileId);
        byte selectedFileIdByte = Byte.parseByte(selectedFileId);
        Log.d(TAG, "changeTheFileSettings for selectedFileId " + selectedFileIdInt);
        Log.d(TAG, printData("DES session key", SESSION_KEY_DES));

        byte changeFileSettingsCommand = (byte) 0x5f;
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
        //byte accessRightsRW = (byte) 0x34; // Read Access & Write Access // read with key 3, write with key 4
        byte accessRightsRW = (byte) 0x22; // Read Access & Write Access // read with key 2, write with key 2
        // to calculate the crc16 over the setting bytes we need a 3 byte long array
        byte[] bytesForCrc = new byte[3];
        bytesForCrc[0] = commSettingsByte;
        bytesForCrc[1] = accessRightsRwCar;
        bytesForCrc[2] = accessRightsRW;
        Log.d(TAG, printData("bytesForCrc", bytesForCrc));
        byte[] crc16Value = CRC16.get(bytesForCrc);
        Log.d(TAG, printData("crc16Value", crc16Value));
        // create a 8 byte long array
        byte[] bytesForDecryption = new byte[8];
        System.arraycopy(bytesForCrc, 0, bytesForDecryption, 0, 3);
        System.arraycopy(crc16Value, 0, bytesForDecryption, 3, 2);
        Log.d(TAG, printData("bytesForDecryption", bytesForDecryption));
        // generate 24 bytes long triple des key
        byte[] tripleDES_SESSION_KEY = new byte[24];
        System.arraycopy(SESSION_KEY_DES, 0, tripleDES_SESSION_KEY, 0, 8);
        System.arraycopy(SESSION_KEY_DES, 0, tripleDES_SESSION_KEY, 8, 8);
        System.arraycopy(SESSION_KEY_DES, 0, tripleDES_SESSION_KEY, 16, 8);
        Log.d(TAG, printData("tripeDES Session Key", tripleDES_SESSION_KEY));
        byte[] IV_DES = new byte[8];
        Log.d(TAG, printData("IV_DES", IV_DES));
        //byte[] decryptedData = TripleDES.encrypt(IV_DES, tripleDES_SESSION_KEY, bytesForDecryption);
        byte[] decryptedData = TripleDES.decrypt(IV_DES, tripleDES_SESSION_KEY, bytesForDecryption);
        Log.d(TAG, printData("decryptedData", decryptedData));
        // the parameter for wrapping
        byte[] parameter = new byte[9];
        parameter[0] = selectedFileIdByte;
        System.arraycopy(decryptedData, 0, parameter, 1, 8);
        Log.d(TAG, printData("parameter", parameter));
        byte[] wrappedCommand;
        byte[] response;
        try {
            wrappedCommand = wrapMessage(changeFileSettingsCommand, parameter);
            Log.d(TAG, printData("wrappedCommand", wrappedCommand));
            response = isoDep.transceive(wrappedCommand);
            Log.d(TAG, printData("response", response));
            if (checkResponse(response)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * section for commit
     */

    private boolean commitWriteToFile(TextView logTextView, byte[] response) {
        // don't forget to commit all changes
        byte commitCommand = (byte) 0xc7;
        byte[] commitResponse = new byte[0];
        byte[] apdu = new byte[0];
        try {
            apdu = wrapMessage(commitCommand, null);
            commitResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("commitResponse", commitResponse));
        System.arraycopy(returnStatusBytes(commitResponse), 0, response, 0, 2);
        if (checkResponse(commitResponse)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * section for value files
     */

    private int readFromValueFile(TextView logTextView, byte fileNumber, byte[] response) {
        // we read from a standard file within the selected application
        byte readValueFileCommand = (byte) 0x6c;
        byte[] readValueFileResponse = new byte[0];
                /*
                // DESFireEv1:
                byte[] apdu = new byte[7];
                apdu[0] = (byte) 0x90;
                apdu[1] = readValueFileCommand;
                apdu[2] = 0x00;
                apdu[3] = 0x00;
                apdu[4] = 0x01;
                apdu[5] = fileNumber;
                apdu[6] = 0x00;
                */
        byte[] apdu = new byte[0];
        try {
            apdu = wrapMessage(readValueFileCommand, new byte[]{fileNumber});
            readValueFileResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return 0;
        }
        if (readValueFileResponse == null) {
            writeToUiAppend(logTextView, "unknown error");
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return 0;
        }
        writeToUiAppend(logTextView, printData("readValueFileResponse", readValueFileResponse));
        // readValueFileResponse length: 6 data: 320000009100
        if (readValueFileResponse.length > 2) {
            System.arraycopy(returnStatusBytes(readValueFileResponse), 0, response, 0, 2);
            byte[] valueBytes = Arrays.copyOf(readValueFileResponse, readValueFileResponse.length - 2);
            int value = Utils.byteArrayLength4InversedToInt(valueBytes);
            writeToUiAppend(logTextView, "Actual value: " + value);
            return value;
        } else if (readValueFileResponse.length == 2) {
            System.arraycopy(returnStatusBytes(readValueFileResponse), 0, response, 0, 2);
            return 0;
        } else {
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return 0;
        }
    }

    private boolean creditValueFile(TextView logTextView, int fileNumber, int changeValue, byte[] response) {
        byte creditValueFileCommand = (byte) 0x0c;
        PayloadBuilder pb = new PayloadBuilder();
        byte[] parameters = pb.creditValueFile(fileNumber, changeValue);
        byte[] creditValueResponse = new byte[0];
        byte[] apdu = new byte[0];
        try {
            apdu = wrapMessage(creditValueFileCommand, parameters);
            creditValueResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("creditValueFileResponse", creditValueResponse));
        // readValueFileResponse length: 6 data: 320000009100
        System.arraycopy(returnStatusBytes(creditValueResponse), 0, response, 0, 2);
        if (checkResponse(creditValueResponse)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean debitValueFile(TextView logTextView, int fileNumber, int changeValue, byte[] response) {
        byte debitValueFileCommand = (byte) 0xdc;
        PayloadBuilder pb = new PayloadBuilder();
        byte[] parameters = pb.creditValueFile(fileNumber, changeValue);
        byte[] debitValueResponse = new byte[0];
        byte[] apdu = new byte[0];
        try {
            apdu = wrapMessage(debitValueFileCommand, parameters);
            debitValueResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("debValueFileResponse", debitValueResponse));
        // readValueFileResponse length: 6 data: 320000009100
        System.arraycopy(returnStatusBytes(debitValueResponse), 0, response, 0, 2);
        if (checkResponse(debitValueResponse)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * section for record files
     */

    private boolean createRecordFile(TextView logTextView, byte fileNumber, int fileSize, int numberOfRecords, boolean isLinearRecordFile, byte[] response) {
        // we create a records file within the selected application
        byte createLinearRecordFileCommand = (byte) 0xc1;
        byte createCyclicRecordFileCommand = (byte) 0xc0;
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

        PayloadBuilder pb = new PayloadBuilder();
        byte createRecordFileCommand;
        byte[] apdu;
        byte[] createRecordFileParameters;
        if (isLinearRecordFile) {
            createRecordFileParameters = pb.createLinearRecordFile(fileNumber, PayloadBuilder.CommunicationSetting.Plain,
                    1, 2, 3, 4, fileSize, numberOfRecords);
            createRecordFileCommand = createLinearRecordFileCommand;
            writeToUiAppend(output, printData("payloadCreateLinearRecordFile", createRecordFileParameters));
        } else {
            createRecordFileParameters = pb.createCyclicRecordFile(fileNumber, PayloadBuilder.CommunicationSetting.Plain,
                    1, 2, 3, 4, fileSize, numberOfRecords);
            createRecordFileCommand = createCyclicRecordFileCommand;
            writeToUiAppend(output, printData("payloadCreateCyclicRecordFile", createRecordFileParameters));
        }
        byte[] createRecordFileResponse = new byte[0];

        try {
            apdu = wrapMessage(createRecordFileCommand, createRecordFileParameters);
            createRecordFileResponse = adapter.sendSimple(apdu);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        System.arraycopy(returnStatusBytes(createRecordFileResponse), 0, response, 0, 2);
        writeToUiAppend(logTextView, printData("createRecordFileResponse", createRecordFileResponse));
        if (checkDuplicateError(createRecordFileResponse)) {
            writeToUiAppend(logTextView, "the file was not created as it already exists, proceed");
            return true;
        }
        if (checkResponse(createRecordFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean writeToRecordFile(TextView logTextView, byte fileNumber, byte[] data, boolean isLinearRecordFile, byte[] response) {

        byte writeRecordFileCommand = (byte) 0x3b;
        PayloadBuilder pb = new PayloadBuilder();
        byte[] writeRecordFileParameter;
        if (isLinearRecordFile) {
            writeRecordFileParameter = pb.writeToLinearRecordFile(fileNumber, data);
        } else {
            // isCyclicRecordFile
            writeRecordFileParameter = pb.writeToCyclicRecordFile(fileNumber, data);
        }
        writeToUiAppend(logTextView, printData("writeRecordFileParameter", writeRecordFileParameter));
        byte[] apdu;
        byte[] writeRecordFileResponse = new byte[0];
        try {
            apdu = wrapMessage(writeRecordFileCommand, writeRecordFileParameter);
            writeRecordFileResponse = adapter.sendRequestChain(apdu);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("writeRecordFileResponse", writeRecordFileResponse));
        System.arraycopy(returnStatusBytes(writeRecordFileResponse), 0, response, 0, 2);
        if (checkResponse(writeRecordFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    private byte[] readFromRecordFile(TextView logTextView, int fileNumber, int firstRecordToRead, int numberOfRecordsToRead, int recordSize, byte[] response) {
        // we read from a record file within the selected application
        byte readRecordFileCommand = (byte) 0xbb;
        PayloadBuilder pb = new PayloadBuilder();
        byte[] readRecordFileParameter = pb.readFromRecordFile(fileNumber, firstRecordToRead, numberOfRecordsToRead);
        writeToUiAppend(logTextView, printData("readRecordFileParameter", readRecordFileParameter));
        byte[] readRecordFileResponse = new byte[0];
        byte[] apdu;
        try {
            apdu = wrapMessage(readRecordFileCommand, readRecordFileParameter);
            writeToUiAppend(logTextView, printData("send APDU", apdu));
            byte[] readRecordFileResponse1st = adapter.sendRequestChain(apdu);
            writeToUiAppend(logTextView, printData("readRecordFileResponse1st", readRecordFileResponse1st));
            if (readRecordFileResponse1st == null) {
                writeToUiAppend(logTextView, "the readRecordFile command failed, aborted");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
                return null;
            }
            readRecordFileResponse = adapter.receiveResponseChain(readRecordFileResponse1st);
            writeToUiAppend(logTextView, printData("readRecordFileResponse2nd", readRecordFileResponse));

        } catch (IOException e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }

        writeToUiAppend(logTextView, printData("readRecordFileResponse", readRecordFileResponse));
        System.arraycopy(adapter.getFullCode(), 0, response, 0, 2);

        // if the card responses more data than expected we truncate the data
        int expectedResponseLength = numberOfRecordsToRead * recordSize;
        if (readRecordFileResponse.length == expectedResponseLength) {
            return readRecordFileResponse;
        } else if (readRecordFileResponse.length > expectedResponseLength) {
            // more data is provided - truncated
            return Arrays.copyOf(readRecordFileResponse, expectedResponseLength);
        } else {
            // less data is provided - we return as much as possible
            return readRecordFileResponse;
        }
    }

    /**
     * section for encrypted standard file handling
     */

    private boolean createStandardFileEncrypted(TextView logTextView, int fileNumber, int fileSize, byte[] response) {
        // we create a standard file within the selected application
        byte createStandardFileCommand = (byte) 0xcd;

        PayloadBuilder pb = new PayloadBuilder();
        byte[] createStandardFileParameter = pb.createStandardFile(fileNumber, PayloadBuilder.CommunicationSetting.Encrypted, 1, 2, 3, 4, fileSize);

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
        writeToUiAppend(logTextView, printData("createStandardFileParameter", createStandardFileParameter));
        byte[] createStandardFileResponse = new byte[0];
        byte[] apdu;
        try {
            apdu = wrapMessage(createStandardFileCommand, createStandardFileParameter);
            createStandardFileResponse = adapter.sendSimple(apdu);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("createStandardFileResponse", createStandardFileResponse));
        System.arraycopy(returnStatusBytes(createStandardFileResponse), 0, response, 0, 2);
        if (checkResponse(createStandardFileResponse)) {
            return true;
        } else {
            return false;
        }
    }

    // note: we don't need to commit any write on Standard Files
    private boolean writeToStandardFileEncrypted(TextView logTextView, int fileNumber, byte[] data, byte[] response) {
        // some sanity checks to avoid any issues
        int fileSize = selectedFileSettings.getFileSizeInt();
        if (fileNumber < (byte) 0x00) return false;
        if (fileNumber > (byte) 0x0F) return false;
        if ((data == null) || (data.length == 0)) return false;
        if (data.length > fileSize) return false;

        Log.d(TAG, printData("data", data));
        // i'm encrypting the data only without any header
        byte[] dataEncrypted = encryptDataDes(data, SESSION_KEY_DES);
        Log.d(TAG, printData("dataEncrypted", dataEncrypted));
        // write to file
        byte writeStandardFileCommand = (byte) 0x3d;
        int offsetBytes = 0;

        PayloadBuilder pb = new PayloadBuilder();
        //byte[] writeStandardFileParameter = pb.writeToStandardFile(fileNumber, data, offsetBytes);
        byte[] writeStandardFileParameter = pb.writeToStandardFile(fileNumber, dataEncrypted, offsetBytes);
        writeToUiAppend(logTextView, printData("writeStandardFileParameter", writeStandardFileParameter));

        byte[] writeStandardFileResponse = new byte[0];
        try {
            // correct the parameter length
            writeStandardFileParameter[4] = (byte) (data.length & 0xff);
            byte[] apdu = wrapMessage(writeStandardFileCommand, writeStandardFileParameter);
            writeToUiAppend(logTextView, printData("plain apdu", apdu));

            // correct the length of data
            //apdu[4] = (byte) (6 + dataEncrypted.length);
            //writeToUiAppend(logTextView, printData("plain apdu", apdu));
            // apdu 18 is the length of data itself
            //apdu[9] = (byte) (data.length &0xff);
            //writeToUiAppend(logTextView, printData("plain apdu", apdu));

            //byte[] apduShort = Arrays.copyOf(apdu, apdu.length - 1); // strip of the last 00
            //writeToUiAppend(logTextView, printData("plain apdu short", apduShort));
            // this is the point where the encryption and adding of crc's takes place
            writeToUiAppend(logTextView, printData("SESSION_KEY_DES", SESSION_KEY_DES));
            //byte[] encryptedApdu = preprocessEncipheredDes(apdu, 12, SESSION_KEY_DES);
            //writeToUiAppend(logTextView, printData("encr. apdu", encryptedApdu));
            //byte[] encryptedApduLong = new byte[encryptedApdu.length + 1];
            //System.arraycopy(encryptedApdu, 0, encryptedApduLong, 0, encryptedApdu.length);
            //writeToUiAppend(logTextView, printData("encr. apdu long ", encryptedApduLong));
            writeStandardFileResponse = adapter.sendRequestChain(apdu);
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

    private byte[] encryptDataDes(byte[] data, byte[] desKey) {
        final int blockSize = 8;
        byte[] crc16 = CRC16.get(data);
        Log.d(TAG, printData("DES key", desKey));
        Log.d(TAG, printData("data", data));
        Log.d(TAG, printData("CRC16", crc16));
        int dataLength = data.length;
        int crc16Length = crc16.length; // should be 2
        int padding = 0;  // padding=0 if block length is adequate
        //int lengthWithCrc =  dataLength + crc16Length;
        if ((dataLength + crc16Length) % blockSize != 0)
            padding = blockSize - (dataLength + crc16Length) % blockSize;
        int ciphertextLength = dataLength + crc16Length + padding;
        Log.d(TAG, "dataLength: " + dataLength + " CRC16Length: " + crc16Length + " padding: " + padding);
        Log.d(TAG, "ciphertextLength: " + ciphertextLength);
        byte[] dataWithCrc = new byte[ciphertextLength];
        System.arraycopy(data, 0, dataWithCrc, 0, dataLength);
        System.arraycopy(crc16, 0, dataWithCrc, dataLength, crc16Length);
        Log.d(TAG, printData("dataWithCrc", dataWithCrc));
        byte[] dataDecrpytionMode = decryptDes(desKey, dataWithCrc);
        Log.d(TAG, printData("dataDecryptionMode", dataDecrpytionMode));
        return dataDecrpytionMode;
    }

    // code taken from NFCjLib DESFireEV1.java but reduced to DES mode only
    // warning: do not use for TDES or AES keys

    // calculate CRC and append, encrypt, and update global IV
    private byte[] preprocessEncipheredDes(byte[] apdu, int offset, byte[] skey) {
        byte[] ciphertext = encryptApduDes(apdu, offset, skey);

        byte[] ret = new byte[5 + offset + ciphertext.length + 1];
        System.arraycopy(apdu, 0, ret, 0, 5 + offset);
        System.arraycopy(ciphertext, 0, ret, 5 + offset, ciphertext.length);
        ret[4] = (byte) (offset + ciphertext.length);

        return ret;
    }

    /* Only data is encrypted. Headers are left out (e.g. keyNo for credit). */
    private static byte[] encryptApduDes(byte[] apdu, int offset, byte[] sessionKey) {
        int blockSize = 8;
        int payloadLen = apdu.length - 6;
        byte[] crc = null;
        crc = calculateApduCRC16C(apdu, offset);

        int padding = 0;  // padding=0 if block length is adequate
        if ((payloadLen - offset + crc.length) % blockSize != 0)
            padding = blockSize - (payloadLen - offset + crc.length) % blockSize;
        int ciphertextLen = payloadLen - offset + crc.length + padding;
        byte[] plaintext = new byte[ciphertextLen];
        System.arraycopy(apdu, 5 + offset, plaintext, 0, payloadLen - offset);
        System.arraycopy(crc, 0, plaintext, payloadLen - offset, crc.length);
        return sendDes(sessionKey, plaintext);
    }

    private static byte[] sendDes(byte[] key, byte[] data) {
        return decryptDes(key, data);
    }

    // CRC16 calculated only over data
    private static byte[] calculateApduCRC16C(byte[] apdu, int offset) {
        if (apdu.length == 5) {
            return CRC16.get(new byte[0]);
        } else {
            return CRC16.get(apdu, 5 + offset, apdu.length - 5 - offset - 1);
        }
    }

    // DES/3DES decryption: CBC send mode and CBC receive mode
    // here fixed to SEND_MODE = decrypt
    private static byte[] decryptDes(byte[] key, byte[] data) {

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

    private byte[] readFromStandardFileEncrypted(TextView logTextView, byte fileNumber, byte[] response) {
        // we read from a standard file within the selected application

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
            writeToUiAppend(logTextView, printData("send APDU", apdu));

            //readStandardFileResponse = adapter.send(apdu);
            byte[] readStandardFileResponse1st = adapter.sendRequestChain(apdu);
            writeToUiAppend(logTextView, printData("readStandardFileResponse1st", readStandardFileResponse1st));
            if (readStandardFileResponse1st == null) {
                writeToUiAppend(logTextView, "the readStandardFile command failed, aborted");
                byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
                System.arraycopy(responseManual, 0, response, 0, 2);
                return null;
            }
            readStandardFileResponse = adapter.receiveResponseChain(readStandardFileResponse1st);
            writeToUiAppend(logTextView, printData("readStandardFileResponse2nd", readStandardFileResponse));

        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return null;
        }
        writeToUiAppend(logTextView, printData("readStandardFileResponse", readStandardFileResponse));
        System.arraycopy(adapter.getFullCode(), 0, response, 0, 2);

        // if the card responses more data than expected we truncate the data
        int expectedResponse = numberOfBytes - offsetBytes;
        if (readStandardFileResponse.length == expectedResponse) {
            return readStandardFileResponse;
        } else if (readStandardFileResponse.length > expectedResponse) {
            // more data is provided - truncated
            return Arrays.copyOf(readStandardFileResponse, expectedResponse);
        } else {
            // less data is provided - we return as much as possible
            return readStandardFileResponse;
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
        //writeToUiAppend(logTextView, "create the application " + Utils.bytesToHex(applicationIdentifier));
        byte createApplicationCommand = (byte) 0xca;
        byte applicationMasterKeySettings = (byte) 0x0f;
        PayloadBuilder pb = new PayloadBuilder();
        byte[] parameters = pb.createApplication(applicationIdentifier, applicationMasterKeySettings, numberOfKeys);
        byte[] createApplicationResponse = new byte[0];
        byte[] apdu = new byte[0];
        try {
            apdu = wrapMessage(createApplicationCommand, parameters);
            createApplicationResponse = adapter.sendSimple(apdu);
        } catch (IOException e) {
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            byte[] responseManual = new byte[]{(byte) 0x91, (byte) 0xFF};
            System.arraycopy(responseManual, 0, response, 0, 2);
            return false;
        }
        writeToUiAppend(logTextView, printData("createApplicationResponse", createApplicationResponse));
        // readValueFileResponse length: 6 data: 320000009100
        System.arraycopy(returnStatusBytes(createApplicationResponse), 0, response, 0, 2);
        if (checkResponse(createApplicationResponse)) {
            return true;
        } else {
            return false;
        }

        /*


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

         */
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
        if (keyNumber > (byte) 0x0f)
            return false; // todo this check is incomplete, use maximum key number from key settings
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
     * @param a       1K/2K/3K 3DES
     * @param offset  start position of the key within a
     * @param length  key length
     * @param version the 1-byte version
     *                Source: DESFireEV1.java (NFCJLIB)
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

    private byte[] wrapMessage(byte command, byte[] parameters) throws IOException {
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
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_focused}, // focused
                    new int[]{android.R.attr.state_hovered}, // hovered
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{}  //
            };
            int[] colors = new int[]{
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
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_focused}, // focused
                new int[]{android.R.attr.state_hovered}, // hovered
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{}  //
        };
        int[] colors = new int[]{
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
        SESSION_KEY_DES = null;
        SESSION_KEY_TDES = null;
    }

    private void invalidateEncryptionKeys() {
        KEY_NUMBER_USED_FOR_AUTHENTICATION = -1;
        SESSION_KEY_DES = null;
        SESSION_KEY_TDES = null;
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