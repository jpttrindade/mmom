package br.com.jpttrindade.mmomlib.mmomserver;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by jpttrindade on 18/06/16.
 */
public class MMomMessageEncoder {

    public static byte[] encode(MMomMessage message){

        int bufferSize = getBufferSize(message);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        int index = writeCode(message.code, buffer, 0);
        index = writeDestinationId(message.destinationId, buffer, index);
       // index = writeRequestorId(message.requestorId, buffer, index);
        index = writeRequestId(message.requestId, buffer, index);
        index = writeType(message.type, buffer, index);
        index = writeFileName(message.fileName, buffer, index);

        if (message.code == 1) {
            index = writeTextContent(message.textContent, buffer, index);
        }

        if (message.code == 2){
            if (message.type == 1){
                index = writeTextContent(message.textContent, buffer, index);
            } else {
                index = writeFileContent(message.fileContent, buffer, index);
            }
        }

        writeEnd(buffer);

        return buffer.array();
    }

    private static void writeEnd(ByteBuffer buffer) {
        buffer.put("\n\n".getBytes(),0,2);
    }

    private static int writeTextContent(String textContent, ByteBuffer buffer, int index) {
        buffer.put((byte)textContent.length());
        index++;
        buffer.put(textContent.getBytes());
        index += textContent.length();
        return index;
    }

    private static int writeFileContent(File fileContent, ByteBuffer buffer, int index) {
        try {
            Log.d("DEBUG", "File size = "+ fileContent.length());
            FileInputStream inputStream = new FileInputStream(fileContent);

            //byte[] contentBuffer = IOUtils.toByteArray(inputStream);
            byte [] contentBuffer = new byte[(int)fileContent.length()];
            inputStream.read(contentBuffer);

            Log.d("DEBUG", "Buffer size = "+ contentBuffer.length);
            buffer.put(contentBuffer);

            index += fileContent.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return index;
    }


    private static int writeCode(int code, ByteBuffer buffer, int index) {
        buffer.put((byte) code);
        index++;
        return index;
    }

    private static int writeRequestorId(String requestorId, ByteBuffer buffer, int index) {
        if (requestorId != null) {
            buffer.put((byte) requestorId.length());
            index++;
            buffer.put(requestorId.getBytes(), 0, requestorId.length());
            index += requestorId.length();
        }
        return index;
    }

    private static int writeRequestId(String requestId, ByteBuffer buffer, int index) {
        if(requestId != null) {
            buffer.put((byte)requestId.length());
            index++;
            buffer.put(requestId.getBytes(),0, requestId.length());
            index += requestId.length();
        }
        return index;
    }

    private static int writeDestinationId(String destinationId, ByteBuffer buffer, int index) {
        if(destinationId != null) {
            buffer.put((byte) destinationId.length());
            index++;
            buffer.put(destinationId.getBytes(),0, destinationId.length());
            index += destinationId.length();
        }
        return index;
    }

    private static int writeType(int type, ByteBuffer buffer, int index) {
        if(type > 0) {
            buffer.put((byte)type);
            index++;
        }
        return index;
    }

    private static int writeFileName(String fileName, ByteBuffer buffer, int index) {
        if(fileName != null) {
            buffer.put((byte) fileName.length());
            index++;
            buffer.put(fileName.getBytes(),0, fileName.length());
            index += fileName.length();
        }
        return index;    }

    public static MMomMessage decode(byte[] _buffer){
        ByteBuffer buffer = ByteBuffer.allocate(_buffer.length);
        buffer.put(_buffer);

        MMomMessage message = new MMomMessage();
        int index = 0;
        try {
            index = readCode(message, buffer, index);
            index = readDestinationId(message, buffer, index);
            index = readRequestorId(message, buffer, index);
            index = readRequestId(message, buffer, index);
            index = readType(message, buffer, index);
            // index = readFileName(message, buffer, index);

            if(message.code == 1){
                index = readTextContent(message, buffer, index);
            }



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }

    private static int readCode(MMomMessage message, ByteBuffer buffer, int index) {

        int code = buffer.get(index);
        message.setCode(code);
        index++;
        Log.d("DEBUG", "code:"+code);
        return index;
    }

    private static int readRequestorId(MMomMessage message, ByteBuffer buffer, int index) {
        if (message.code == 1 ) {
            try {
                int requestorIdSize = buffer.get(index);
                index++;
                String requestorId = new String(Arrays.copyOfRange(buffer.array(), index, index+requestorIdSize), "UTF-8");
                message.setRequestorId(requestorId);
                index += requestorIdSize;
                Log.d("DEBUG", "requestorId:"+requestorId);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return index;
    }

    private static int readRequestId(MMomMessage message, ByteBuffer buffer, int index) {
        if(message.code > 0) {
            try {
                int requestIdSize = buffer.get(index);
                index++;
                String requestId = new String(Arrays.copyOfRange(buffer.array(),index, index+requestIdSize), "UTF-8");
                message.setRequestId(requestId);
                index += requestIdSize;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    private static int readDestinationId(MMomMessage message, ByteBuffer buffer, int index) throws UnsupportedEncodingException {
        if(message.code > 0) {
            int destinationIdSize = buffer.get(index);
            index++;
            String destinationId = new String(Arrays.copyOfRange(buffer.array(),index, index+destinationIdSize), "UTF-8");
            message.setDestinationId(destinationId);
            index += destinationIdSize;
            Log.d("DEBUG", "destinationId:"+destinationId);
        }
        return index;
    }



    private static int readType(MMomMessage message, ByteBuffer buffer, int index) {
        if(message.code == 2) {
            int type = buffer.get(index);
            message.setType(type);
            index++;
        }
        return index;
    }

/*    private static int readFileName(MMomMessage message, byte[] buffer, int index) throws UnsupportedEncodingException {
        if (message.code == 2) {
            int fileNameSize = buffer[index];
            index++;
            String fileName = null;
            fileName = new String(Arrays.copyOfRange(buffer, index, index+fileNameSize), "UTF-8");
            message.setFileName(fileName);
            index += fileNameSize;
        }

        return index;
    }


    private static int readFileContent(MMomMessage message, byte[] buffer, int index) throws UnsupportedEncodingException {

        return index;
    }*/

    private static int readTextContent(MMomMessage message, ByteBuffer buffer, int index) throws UnsupportedEncodingException {
        int contentSize = buffer.get(index);
        index++;
        String content = new String(Arrays.copyOfRange(buffer.array(), index, index+contentSize), "UTF-8");
        message.setTextContent(content);
        index += contentSize;
        return index;
    }


    private static int getBufferSize(MMomMessage message) {
        int size = 1 /*code*/;
        if (message.requestorId != null) {
            size += 1 + message.requestorId.length();
        }
        if (message.requestId != null) {
            size += 1 /*requestId size*/ + message.requestId.length()/*requestId*/;
        }
        if (message.destinationId != null) {
            size += 1 /*requestId size*/ + message.destinationId.length() /*requestId*/;
        }
        if (message.type >= 0) {
            size += 1;
        }
        if (message.fileName != null){
            size += 1 /*fileName size*/+ message.fileName.length();/*fileName*/
        }



        if(/*message.code == 1 ||*/ message.type == 1){

		/*content size*/
            size++;
            size += message.textContent.length() /*content*/;
        }

        if(message.type == 2 ) {
            size += message.fileContent.length() /*content*/;
        }

        size += 2; /* end size */

        return size;

    }

}
