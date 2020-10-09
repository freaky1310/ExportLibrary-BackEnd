package exportLibrary;

import formModels.CurriculumForm;
import formModels.Form;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;


public class Utils {

    private final DocExt[] supportedExts;

    public Utils() {
        this.supportedExts = DocExt.values();
    }

    /**
     * Replaces fields values in the template
     * @param fieldValues A Form containing the template fields values
     * @param docName the name of the template
     * @param inExt the input extension of the file
     * @param outExt A DocExt containing the output file extension
     */
    public File insertFields(JSONArray fieldValues, String docName, DocExt inExt, DocExt outExt) {

        if(Arrays.asList(this.supportedExts).contains(inExt)) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream("templates/" + docName));
                IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
                IContext context = report.createContext();
                for (Object fieldValue : fieldValues) {
                    JSONObject currentObj = (JSONObject) fieldValue;
                    context.put(currentObj.get("label").toString(), currentObj.get("value"));
                }

                File outFile = new File(docName);
                OutputStream out = new FileOutputStream(outFile);
                report.process(context, out);

                return outFile;

            } catch (IOException | XDocReportException e) {
                e.printStackTrace();
            }
        }
        else {
            // This should throw something like "FileNotSupportedException"
        }
        return null;
    }

    /**
     * Simple utility method to get the file extension from its name
     * @param fileName the name of the file
     * @return DocExt the extension as an enumeration
     */
    public static DocExt getFileExtension(String fileName) {

        String extension = "";
        int index = fileName.lastIndexOf('.');
        if(index > 0) {
            extension = fileName.substring(index + 1);
        }
        if(extension.equals("docx")) {
            return DocExt.DOCX;
        }
        else if(extension.equals("xlsx")) {
            return DocExt.XLSX;
        }
        else {
            return null;
        }
    }

    /**
     * Works as a mapper from Json to Java class using the JSON Simple API methods
     * @return Form a compiled Form corresponding to the given Json String
     */
    public Form mapJsonToObject(JSONArray jArray) {
        // TODO: generalize to other Forms
        Form cf = new CurriculumForm("tmp", "Curriculum");
        Field[] fields = cf.getClass().getDeclaredFields();
        Iterator jsonArrayItr = jArray.iterator();

        while(jsonArrayItr.hasNext()) {
            for(Field f : fields) {
                JSONObject nextObj = (JSONObject)jsonArrayItr.next();
                if(nextObj.get("label").equals(f.getName())) {
                    try {
                        Field currentField = cf.getClass().getDeclaredField(f.getName());
                        // TODO: look for a better solution
                        currentField.setAccessible(true);
                        currentField.set(cf, nextObj.get("value"));
                        currentField.setAccessible(false);
                    } catch(NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return cf;
    }

}
