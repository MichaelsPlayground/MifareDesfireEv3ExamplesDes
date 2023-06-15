package de.androidcrypto.mifaredesfireev3examplesdes;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

/**
 * This class takes the PICC response of the getFileSettings command (0x5f)
 */

public class FileSettings {

    private byte fileNumber;
    private byte fileType;
    private String fileTypeName;
    private byte communicationSettings;
    private String communicationSettingsName;
    private byte accessRightsRwCar; // Right & Write access key | Change access key
    private byte accessRightsRW; // Read access key | Write access key
    private int accessRightsRw, accessRightsCar, accessRightsR, accessRightsW;
    private byte[] fileSize; // 3 bytes, available for Standard & Backup files only
    // the following variables are available for value files only
    private byte[] valueMin;
    private byte[] valueMax;
    private byte[] valueLimitedCredit;
    private byte valueLimitedCreditAvailable;
    // the following variables are available for linear record and cyclic record files only
    private byte[] recordSize; // 3 bytes
    private byte[] recordsMax; // 3 bytes
    private byte[] recordsExisting; // 3 bytes
    private byte[] completeResponse; // the complete data returned on getFileSettings command

    public FileSettings(byte fileNumber, byte[] completeResponse) {
        this.fileNumber = fileNumber;
        this.completeResponse = completeResponse;
        if (completeResponse == null) return;
        if (completeResponse.length < 7) return;
        analyze();
    }

    private void analyze() {
        int position = 0;
        fileType = completeResponse[0]; // needed to know the kind of variables to fill
        fileTypeName = getFileTypeName(fileType);
        position ++;
        communicationSettings = completeResponse[position];
        position ++;
        if (communicationSettings == (byte) 0x00) communicationSettingsName = "Plain";
        if (communicationSettings == (byte) 0x01) communicationSettingsName = "CMACed";
        if (communicationSettings == (byte) 0x03) communicationSettingsName = "Encrypted";
        accessRightsRwCar = completeResponse[position];
        position ++;
        accessRightsRW = completeResponse[position];
        position ++;
        // todo get the values vor RW, Car, R and W
        if ((fileType == (byte) 0x00) | (fileType == (byte) 0x01)) {
            // standard and backup file
            fileSize = Arrays.copyOfRange(completeResponse, position, position + 3);
            return;
        }
        if (fileType == (byte) 0x02) {
            // value file
            valueMin = Arrays.copyOfRange(completeResponse, position, position + 4);
            position += 4;
            valueMax = Arrays.copyOfRange(completeResponse, position, position + 4);
            position += 4;
            valueLimitedCredit = Arrays.copyOfRange(completeResponse, position, position + 4);
            position += 4;
            valueLimitedCreditAvailable = completeResponse[position];
            return;
        }
        if ((fileType == (byte) 0x03) | (fileType == (byte) 0x04)) {
            // linear record and cyclic record file
            recordSize = Arrays.copyOfRange(completeResponse, position, position + 3);
            position += 3;
            recordsMax = Arrays.copyOfRange(completeResponse, position, position + 3);
            position += 3;
            recordsExisting = Arrays.copyOfRange(completeResponse, position, position + 3);
            return;
        }
    }

    private String getFileTypeName(byte fileType) {
        switch (fileType) {
            case (byte) 0x00: return "Standard";
            case (byte) 0x01: return "Backup";
            case (byte) 0x02: return "Value";
            case (byte) 0x03: return "Linear Record";
            case (byte) 0x05: return "Cyclic Record";
            default: return "Unknown";
        }
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("fileNumber: ").append(byteToHex(fileNumber)).append("\n");
        sb.append("fileType: ").append(fileType).append(" (").append(fileTypeName).append(")").append("\n");
        sb.append("communicationSettings: ").append(byteToHex(communicationSettings)).append(" (").append(communicationSettingsName).append(")").append("\n");
        sb.append("accessRights RW | CAR: ").append(byteToHex(accessRightsRwCar)).append("\n");
        sb.append("accessRights R | W: ").append(byteToHex(accessRightsRW)).append("\n");
        // todo get the access keys in a single way
        if ((fileType == (byte) 0x00) | (fileType == (byte) 0x01)) {
            sb.append("fileSize: ").append(byteArrayLength3InversedToInt(fileSize)).append("\n");
        }
        if (fileType == (byte) 0x02) {
            sb.append("valueMin: ").append(byteArrayLength4InversedToInt(valueMin)).append("\n");
            sb.append("valueMax: ").append(byteArrayLength4InversedToInt(valueMax)).append("\n");
            sb.append("valueLimitedCredit: ").append(byteArrayLength4InversedToInt(valueLimitedCredit)).append("\n");
            sb.append("valueLimitedCreditAvailable: ").append(byteToHex(valueLimitedCreditAvailable)).append("\n");
        }
        if ((fileType == (byte) 0x03) | (fileType == (byte) 0x04)) {
            sb.append("recordSize: ").append(byteArrayLength3InversedToInt(recordSize)).append("\n");
            sb.append("recordsMax: ").append(byteArrayLength3InversedToInt(recordsMax)).append("\n");
            sb.append("recordsExisting: ").append(byteArrayLength3InversedToInt(recordsExisting)).append("\n");
        }
        return sb.toString();
    }

    private String byteToHex(Byte input) {
        return String.format("%02X", input);
    }

    private int byteArrayLength3InversedToInt(byte[] data) {
        return (data[2] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[0] & 0xff);
    }

    public static int byteArrayLength4InversedToInt(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }
}
