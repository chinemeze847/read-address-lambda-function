package readaddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class reads objects dropped in an s3 bucket and
 * passes what it reads to a lambda function that writes
 * the address to an s3 bucket
 *
 */
public class App implements RequestHandler<S3Event, String>
{

    private static final String TARGET_LAMBDA_FUNCTION_NAME = "arn:aws:lambda:us-east-1:292755305053:function:write-address-to-dynamoDB-WriteAddressFunction-7PVmJjorCxCh";

    private final AmazonS3 s3Client;
    private final AWSLambda lambdaClient;
    private final ObjectMapper objectMapper;

    public App() {
        s3Client = AmazonS3ClientBuilder.defaultClient();
        lambdaClient = AWSLambdaClientBuilder.defaultClient();
        objectMapper = new ObjectMapper();
    }

    /**
     *
     * @param s3Event that causes the function to be triggered
     * @param context the context object
     * @return success if it succeeds
     */

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        try {
            //Gets the records of the s3 bucket when a PUT event occurs
            //and stores it inside a list
            List<S3EventNotificationRecord> records = s3Event.getRecords();

            for (S3EventNotificationRecord record : records) {
                //gets the bucket name
                String bucketName = record.getS3().getBucket().getName();

                //gets the filename(s) that caused the event
                String fileName = record.getS3().getObject().getKey();

                //log output to test if it was retrieved
                context.getLogger().log("BucketName ::: " + bucketName );
                context.getLogger().log("fileName ::: " + fileName );

                //pass the bucket name and filename to the processFile function
                processFile(bucketName, fileName);
            }
            return "Success";
        } catch (Exception e) {
            // Handle any exceptions and return an error message if needed
            return "Error: " + e.getMessage();
        }
    }

    /**
     *
     * @param bucketName the name of the bucket
     * @param fileName the name of the file that caused the event
     * @throws IOException
     */
    private void processFile(String bucketName, String fileName) throws IOException {
        //retrieve the s3 bucket object
        S3Object s3Object = s3Client.getObject(bucketName, fileName);

        //This gets the contents of the bucket and store it into an inputstream
        S3ObjectInputStream inputStream = s3Object.getObjectContent();

        //create a bufferedReader to allow the reading of the file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            //reads every line and stores it in line variable
            while ((line = reader.readLine()) != null) {

                //splits the line variable into an array of words delimited by comma
                String[] addressParts = line.split(",");

                //Build an address variable from the addressParts
                Address address = new Address(Integer.parseInt(addressParts[0]),addressParts[1],addressParts[2],
                        addressParts[3],addressParts[4]);

                System.out.println(address); //log out the address

                //passes the address to the addAddressToDynamoDB
                addAddressToDynamoDB(address);
            }
        }
    }

    private void addAddressToDynamoDB(Address address) throws IOException {
        // Create a request object for invoking the target Lambda function
        InvokeRequest request = new InvokeRequest()
                .withFunctionName(TARGET_LAMBDA_FUNCTION_NAME)
                .withPayload(objectMapper.writeValueAsString(address));

        // Invoke the target Lambda function
        InvokeResult result = lambdaClient.invoke(request);
    }
}