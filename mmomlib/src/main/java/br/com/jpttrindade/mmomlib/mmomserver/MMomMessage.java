package br.com.jpttrindade.mmomlib.mmomserver;

import java.io.File;

/**
 * Created by jpttrindade on 18/06/16.
 */
public class MMomMessage {

    public static final int CODE_CONNECTION = 0;
    public static final int CODE_REQUEST = 1;
    public static final int CODE_RESPONSE = 2;
    public static final int TEXT = 1;
    public static final int FILE = 2;


    public int code;
    public int type;
    public String requestorId;
    public String requestId;
    public String destinationId;
    public String fileName;
    public String textContent;
    public File fileContent;


    public MMomMessage() {
        code = -1;
        type = -1;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileContent(File content) {
        this.fileContent = content;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        return String.format("{ code: %d,\nrequestorId: %s,\nrequestId: %s,\ndestinationId: %s,\ntype: %d,\nfileName: %s,\ntextContent: %s,\nfileConent: ",
                code, requestorId, requestId, destinationId, type, fileName, textContent, fileContent);
    }
}
