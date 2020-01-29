/*
 * Copyright (C) 2015 AnySoftKeyboard
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

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class MainClass {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        if (args.length != 3) {
            System.out.println("Usage: makedictionary [path-to-input-file] [path-to-pack-resource-folder] [prefix]");
            System.exit(1);
        }

        final File inputFile = new File(args[0]);
        final File resourcesFolder = new File(args[1]);

        buildDictionary(inputFile, resourcesFolder, args[2]);
    }

    public static void buildDictionary(final File inputFile, final File resourcesFolder, final String prefix) throws IOException, ParserConfigurationException, SAXException {
        if (!inputFile.isFile() || !inputFile.exists()) {
            throw new IllegalArgumentException("Could not find input file " + inputFile);
        }
        final File tempOutputFile = File.createTempFile("make_dictionary_temp", "bin");

        if (!resourcesFolder.isDirectory() || !resourcesFolder.exists()) {
            throw new IllegalArgumentException("Could not find resource folder " + resourcesFolder);
        }

        final File outputFolder = new File(resourcesFolder, "raw/");
        final File dict_id_array = new File(resourcesFolder, "values/" + prefix + "_words_dict_array.xml");

        System.out.println("Reading words from input " + inputFile.getAbsolutePath());
        System.out.println("Will store output files under " + outputFolder.getAbsolutePath() + ". Created raw folder? " + outputFolder.mkdirs());
        System.out.println("Deleting previous versions...");

        //deleting current files
        tempOutputFile.delete();
        File[] dictFiles = outputFolder.listFiles((dir, name) -> name.endsWith(".dict"));
        if (dictFiles != null && dictFiles.length > 0) {
            for (File file : dictFiles) {
                file.delete();
            }
        }
        dict_id_array.delete();

        MakeBinaryDictionary maker = new MakeBinaryDictionary(inputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath());
        maker.makeDictionary();

        if (!tempOutputFile.exists()) {
            throw new IOException("Failed to create binary dictionary file.");
        }

        if (tempOutputFile.length() <= 0) {
            throw new IOException("Failed to create binary dictionary file. Size zero.");
        }

        //now, if the file is larger than 1MB, I'll need to split it to 1MB chunks and rename them.
        BinaryDictionaryResourceNormalizer normalizer = new BinaryDictionaryResourceNormalizer(tempOutputFile, outputFolder, dict_id_array, prefix);
        normalizer.writeDictionaryIdsResource();

        System.out.println("Done.");
    }
}
