package com.vitco.util.misc;

import com.vitco.core.container.HackedObjectInputStream;
import com.vitco.manager.error.ErrorHandlerInterface;

import java.io.*;
import java.util.HashMap;

/**
 * Some basic tools to deal with files and streams.
 */
public class FileTools {

    public static String readFileAsString(File file, ErrorHandlerInterface errorHandler){
        String result = "";
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            StringBuilder str = new StringBuilder();
            String line = br.readLine();
            while (line != null)
            {
                str.append(line).append("\n");
                line = br.readLine();
            }
            result = str.toString();
        } catch (FileNotFoundException e) {
            errorHandler.handle(e);
        } catch (IOException e) {
            errorHandler.handle(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    errorHandler.handle(e);
                }
            }
        }
        return result;
    }

    // de-serialize object from file
    public static Object loadFromFile(final File file, ErrorHandlerInterface errorHandler) {
        final Object[] result = {null};
        if (file != null && file.exists()) {
            try {
                new AutoFileCloser() {
                    @Override protected void doWork() throws Throwable {
                        InputStream inputStream = autoClose(new FileInputStream( file ));
                        InputStream buffer = autoClose(new BufferedInputStream( inputStream ));
                        ObjectInput input = autoClose(new HackedObjectInputStream( buffer ));
                        result[0] = input.readObject();
                    }
                };
            } catch (RuntimeException e) {
                errorHandler.handle(e);
            }
        }
        return result[0];
    }

    // serialize object to file
    public static boolean saveToFile(final File file, final Object object, ErrorHandlerInterface errorHandler) {
        final boolean[] result = {false};
        try {
            new AutoFileCloser() {
                @Override protected void doWork() throws Throwable {
                    OutputStream outputStream = autoClose(new FileOutputStream( file ));
                    OutputStream buffer = autoClose(new BufferedOutputStream( outputStream ));
                    ObjectOutput output = autoClose(new ObjectOutputStream( buffer ));

                    output.writeObject(object);
                    result[0] = true;
                }
            };
        } catch (RuntimeException e) {
            errorHandler.handle(e);
        }
        return result[0];
    }

    // convert inputstream to string
    public static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        // remove the last line break (was not in file!)
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        bufferedReader.close();
        return stringBuilder.toString();
    }
    // Generic Helper to keep casts safe when de-serializing hash-maps.
    // "prevent compiler warning by explicitly casting"
    public static <K, V> HashMap<K, V> castHash(HashMap input,
                                                Class<K> keyClass,
                                                Class<V> valueClass) {
        HashMap<K, V> output = new HashMap<K, V>();
        if (input == null)
            return output;
        for (Object key: input.keySet().toArray()) {
            if ((key == null) || (keyClass.isAssignableFrom(key.getClass()))) {
                Object value = input.get(key);
                if ((value == null) || (valueClass.isAssignableFrom(value.getClass()))) {
                    K k = keyClass.cast(key);
                    V v = valueClass.cast(value);
                    output.put(k, v);
                } else {
                    throw new AssertionError(
                            "Cannot cast to HashMap<"+ keyClass.getSimpleName()
                                    +", "+ valueClass.getSimpleName() +">"
                                    +", value "+ value +" is not a "+ valueClass.getSimpleName()
                    );
                }
            } else {
                throw new AssertionError(
                        "Cannot cast to HashMap<"+ keyClass.getSimpleName()
                                +", "+ valueClass.getSimpleName() +">"
                                +", key "+ key +" is not a " + keyClass.getSimpleName()
                );
            }
        }
        return output;
    }

    // change the extension of a file e.g. "test.txt", ".dat" -> "test.dat"
    public static String changeExtension(String originalName, String newExtension) {
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot != -1) {
            return originalName.substring(0, lastDot) + newExtension;
        } else {
            return originalName + newExtension;
        }
    }
}
