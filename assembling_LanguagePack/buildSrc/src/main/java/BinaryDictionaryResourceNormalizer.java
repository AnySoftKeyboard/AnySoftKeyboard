/*
 * Copyright (C) 2016 AnySoftKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;

/**
 * Compresses a list of words and frequencies into a tree structured binary dictionary.
 */
class BinaryDictionaryResourceNormalizer {
    private static final int DICT_FILE_CHUNK_SIZE = 1000 * 1000;
    private final File tempOutputFile;
    private final File outputFolder;
    private final File dict_id_array;
    private final String mPrefix;

    public BinaryDictionaryResourceNormalizer(File tempOutputFile, File outputFolder, File dict_id_array, String prefix) {
        this.tempOutputFile = tempOutputFile;
        this.outputFolder = outputFolder;
        this.dict_id_array = dict_id_array;
        mPrefix = prefix;
    }

    public void writeDictionaryIdsResource() throws IOException {
        splitOutputFile(tempOutputFile, outputFolder);
    }


    private int splitOutputFile(final File tempOutputFile,
                                final File outputFolder) throws IOException {
        //output should be words_1.dict....words_n.dict
        InputStream inputStream = new FileInputStream(tempOutputFile);
        int file_postfix = 0;
        int current_output_file_size = 0;
        byte[] buffer = new byte[4 * 1024];
        OutputStream outputStream = null;
        int read = 0;
        XmlWriter xml = new XmlWriter(dict_id_array);
        xml.writeEntity("resources");
        xml.writeEntity("array").writeAttribute("name", mPrefix +"_words_dict_array");

        while ((read = inputStream.read(buffer)) > 0) {
            if (outputStream != null && current_output_file_size >= DICT_FILE_CHUNK_SIZE) {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }

            if (outputStream == null) {
                file_postfix++;
                xml.writeEntity("item").writeText("@raw/" + mPrefix + "_words_" +file_postfix).endEntity();
                current_output_file_size = 0;
                File chunkFile = new File(outputFolder, mPrefix + "_words_" + file_postfix + ".dict");
                outputStream = new FileOutputStream(chunkFile);
                System.out.println("Writing to dict file " + chunkFile.getAbsolutePath());
            }

            outputStream.write(buffer, 0, read);
            current_output_file_size += read;
        }

        xml.endEntity();
        xml.endEntity();
        xml.close();

        inputStream.close();
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
        System.out.println("Done. Wrote " + file_postfix + " files.");

        return file_postfix;
    }
}