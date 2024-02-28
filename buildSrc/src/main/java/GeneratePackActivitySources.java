import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import groovy.lang.Closure;

public class GeneratePackActivitySources extends DefaultTask {
    private File screenshotFile;
    private File titleFile;
    private File descriptionFile;
    private File websiteFile;
    private File releaseNotesFile;
    private File outputResFolder;
    private File outputSrcFolder;
    private File outputManifestFile;
    private File outputTestSrcFolder;

    @InputFile
    @PathSensitive(RELATIVE)
    public File getScreenshotFile() {
        return screenshotFile;
    }

    public void setScreenshotFile(File screeenshotFile) {
        this.screenshotFile = screeenshotFile;
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getTitleFile() {
        return titleFile;
    }

    public void setTitleFile(File file) {
        this.titleFile = file;
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(File file) {
        this.descriptionFile = file;
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getWebsiteFile() {
        return websiteFile;
    }

    public void setWebsiteFile(File file) {
        this.websiteFile = file;
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getReleaseNotesFile() {
        return releaseNotesFile;
    }

    public void setReleaseNotesFile(File file) {
        this.releaseNotesFile = file;
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getActivityTemplateFile() {
        return new File(getProject().getRootDir(), "addons/StoreStuff/assets/MainActivity.java.template");
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getActivityTestTemplateFile() {
        return new File(getProject().getRootDir(), "addons/StoreStuff/assets/MainActivityTest.java.template");
    }

    @InputFile
    @PathSensitive(RELATIVE)
    public File getManifestTemplateFile() {
        return new File(getProject().getRootDir(), "addons/StoreStuff/assets/AndroidManifest.xml.addon.template");
    }

    @OutputDirectory
    public File getOutputResFolder() {
        return outputResFolder;
    }

    public void setOutputResFolder(File outputResFolder) {
        this.outputResFolder = outputResFolder;
    }
    @OutputDirectory
    public File getOutputSrcFolder() {
        return outputSrcFolder;
    }

    public void setOutputSrcFolder(File outputSrcFolder) {
        this.outputSrcFolder = outputSrcFolder;
    }
    @OutputDirectory
    public File getOutputTestSrcFolder() {
        return outputTestSrcFolder;
    }

    public void setOutputTestSrcFolder(File outputSrcFolder) {
        this.outputTestSrcFolder = outputSrcFolder;
    }

    @OutputFile
    public File getOutputManifestPath() {
        return outputManifestFile;
    }

    public void setOutputManifestPath(File file) {
        this.outputManifestFile = file;
    }

    @TaskAction
    public void generatePackActivitySources() throws Exception {
        createActivityJavaSourceFile();
        createActivityTestJavaSourceFile();
        createApkManifestSourceFile();
        copyScreenshotResourceFile();
        createStringResourceFile(getTitleFile(),  new File(getOutputResFolder(), "values/title.xml"), "app_name");
        createStringResourceFile(getDescriptionFile(),  new File(getOutputResFolder(), "values/description.xml"), "app_description");
        createStringResourceFile(getWebsiteFile(),  new File(getOutputResFolder(), "values/website.xml"), "app_web_site");
        createStringResourceFile(getReleaseNotesFile(),  new File(getOutputResFolder(), "values/release_notes.xml"), "app_release_notes");
    }

    private static void clearTargetFile(File targetFile) throws IOException {
        var parentFolder = targetFile.getParentFile();

        if (!parentFolder.isDirectory()) {
            if (!parentFolder.mkdirs()) {
                throw new IOException("Failed to create output folder " + parentFolder.getAbsolutePath());
            }
        }
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException("Failed to delete old output file at " + targetFile.getAbsolutePath());
        }
    }

    private void copyScreenshotResourceFile() throws IOException {
        var resTarget = new File(getOutputResFolder(), "drawable-nodpi/screenshot.png");
        clearTargetFile(resTarget);
        Files.copy(getScreenshotFile().toPath(), resTarget.toPath());
    }

    private static void createStringResourceFile(File sourceFile, File targetFile, String stringId) throws Exception {
        clearTargetFile(targetFile);

        var text = Files.readString(sourceFile.toPath());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);
        Element rootElement = doc.createElement("resources");
        doc.appendChild(rootElement);

        Element stringElement = doc.createElement("string");
        stringElement.setAttribute("name", stringId);
        stringElement.appendChild(doc.createCDATASection(text.replace("'", "\\'")));
        rootElement.appendChild(stringElement);

        try(var writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(writer);

            transformer.transform(source, result);
        }
    }

    private void createActivityJavaSourceFile() throws IOException {
        var srcTarget = new File(getOutputSrcFolder(), "MainActivity.java");
        clearTargetFile(srcTarget);

        Files.writeString(
                srcTarget.toPath(),
                Files.readString(getActivityTemplateFile().toPath())
                        .replace("{{PACKAGE}}", getProject().getGroup().toString())
                );
    }

    private void createActivityTestJavaSourceFile() throws IOException {
        var srcTarget = new File(getOutputTestSrcFolder(), "MainActivityTest.java");
        clearTargetFile(srcTarget);

        Files.writeString(
                srcTarget.toPath(),
                Files.readString(getActivityTestTemplateFile().toPath())
                        .replace("{{PACKAGE}}", getProject().getGroup().toString())
        );
    }

    private void createApkManifestSourceFile() throws IOException {
        clearTargetFile(getOutputManifestPath());

        Files.writeString(
                getOutputManifestPath().toPath(),
                Files.readString(getManifestTemplateFile().toPath())
                );
    }

}
