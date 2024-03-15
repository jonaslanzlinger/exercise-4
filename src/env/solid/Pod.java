package solid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * A CArtAgO artifact that agent can use to interact with LDP containers in a
 * Solid pod.
 */
public class Pod extends Artifact {

    private String podURL; // the location of the Solid pod

    /**
     * Method called by CArtAgO to initialize the artifact.
     *
     * @param podURL The location of a Solid pod
     */
    public void init(String podURL) {
        this.podURL = podURL;
        log("Pod artifact initialized for: " + this.podURL);
    }

    /**
     * CArtAgO operation for creating a Linked Data Platform container in the Solid
     * pod
     *
     * @param containerName The name of the container to be created
     * 
     */
    @OPERATION
    public void createContainer(String containerName) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("-X");
        command.add("PUT");
        command.add("-H");
        command.add("Content-Type: text/turtle");
        command.add("-d");
        command.add("");
        command.add(podURL + containerName + "/");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * CArtAgO operation for publishing data within a .txt file in a Linked Data
     * Platform container of the Solid pod
     * 
     * @param containerName The name of the container where the .txt file resource
     *                      will be created
     * @param fileName      The name of the .txt file resource to be created in the
     *                      container
     * @param data          An array of Object data that will be stored in the .txt
     *                      file
     */
    @OPERATION
    public void publishData(String containerName, String fileName, Object[] data) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("-X");
        command.add("PUT");
        command.add("-H");
        command.add("Content-Type: text/plain");
        command.add("-d");
        command.add(createStringFromArray(data));
        command.add(podURL + containerName + "/" + fileName);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * CArtAgO operation for reading data of a .txt file in a Linked Data Platform
     * container of the Solid pod
     * 
     * @param containerName The name of the container where the .txt file resource
     *                      is located
     * @param fileName      The name of the .txt file resource that holds the data
     *                      to be read
     * @param data          An array whose elements are the data read from the .txt
     *                      file
     */
    @OPERATION
    public void readData(String containerName, String fileName, OpFeedbackParam<Object[]> data) {
        data.set(readData(containerName, fileName));
    }

    /**
     * Method for reading data of a .txt file in a Linked Data Platform container of
     * the Solid pod
     * 
     * @param containerName The name of the container where the .txt file resource
     *                      is located
     * @param fileName      The name of the .txt file resource that holds the data
     *                      to be read
     * @return An array whose elements are the data read from the .txt file
     */
    public Object[] readData(String containerName, String fileName) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("-H");
        command.add("Accept: text/plain");
        command.add(podURL + containerName + "/" + fileName);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
            String data = new String(process.getInputStream().readAllBytes());
            return createArrayFromString(data);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new Object[0];
    }

    /**
     * Method that converts an array of Object instances to a string,
     * e.g. the array ["one", 2, true] is converted to the string "one\n2\ntrue\n"
     *
     * @param array The array to be converted to a string
     * @return A string consisting of the string values of the array elements
     *         separated by "\n"
     */
    public static String createStringFromArray(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : array) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Method that converts a string to an array of Object instances computed by
     * splitting the given string with delimiter "\n"
     * e.g. the string "one\n2\ntrue\n" is converted to the array ["one", "2",
     * "true"]
     *
     * @param str The string to be converted to an array
     * @return An array consisting of string values that occur by splitting the
     *         string around "\n"
     */
    public static Object[] createArrayFromString(String str) {
        return str.split("\n");
    }

    /**
     * CArtAgO operation for updating data of a .txt file in a Linked Data Platform
     * container of the Solid pod
     * The method reads the data currently stored in the .txt file and publishes in
     * the file the old data along with new data
     * 
     * @param containerName The name of the container where the .txt file resource
     *                      is located
     * @param fileName      The name of the .txt file resource that holds the data
     *                      to be updated
     * @param data          An array whose elements are the new data to be added in
     *                      the .txt file
     */
    @OPERATION
    public void updateData(String containerName, String fileName, Object[] data) {
        Object[] oldData = readData(containerName, fileName);
        Object[] allData = new Object[oldData.length + data.length];
        System.arraycopy(oldData, 0, allData, 0, oldData.length);
        System.arraycopy(data, 0, allData, oldData.length, data.length);
        publishData(containerName, fileName, allData);
    }
}
