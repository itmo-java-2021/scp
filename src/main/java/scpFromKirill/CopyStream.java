package scp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyStream {
    public static void copyStream(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[32*1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0)
        {
            output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.close();
    }
}
