package com.sungardas.init;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.sun.management.UnixOperatingSystemMXBean;
import com.sungardas.snapdirector.dto.InitConfigurationDto;
import com.sungardas.snapdirector.exception.ConfigurationException;
import com.sungardas.snapdirector.exception.DataAccessException;
import com.sungardas.snapdirector.exception.SnapdirectorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@Service
class CredentialsServiceImpl implements CredentialsService {
    private final String catalinaHomeEnvPropName = "catalina.home";
    private final String confFolderName = "conf";
    private final String propFileName = "amazon.properties";
    private final String accessKeyPropName = "amazon.aws.accesskey";
    private final String secretKeyPropName = "amazon.aws.secretkey";
    private static final String AMAZON_S3_BUCKET = "amazon.s3.bucket";
    private static final String AMAZON_SDFS_SIZE = "amazon.sdfs.size";
    private static final String AMAZON_AWS_REGION = "amazon.aws.region";
    private static final String SUNGARGAS_WORKER_CONFIGURATION = "sungardas.worker.configuration";
    private static final Logger LOG = LogManager.getLogger(CredentialsServiceImpl.class);
    private static final long bytesInGB = 1073741824;
    private static final long defaultChunkSize = 4096;
    private AWSCredentials credentials = null;
    private final String DEFAULT_LOGIN = "admin@snapdirector";
    private String instanceId;

    private InitConfigurationDto initConfigurationDto=null;

    @PostConstruct
    private void init(){
        instanceId = getInstanceId();
    }


    @Override
    public void setCredentialsIfValid(@NotNull CredentialsDto credentials) {
        validateCredentials(credentials.getAwsPublicKey(), credentials.getAwsSecretKey());
        this.credentials = new BasicAWSCredentials(credentials.getAwsPublicKey(), credentials.getAwsSecretKey());
    }

    @Override
    public void storeCredentials() {
        validateCredentials(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
        Properties properties = new Properties();
        File file = Paths.get(System.getProperty(catalinaHomeEnvPropName), confFolderName, propFileName).toFile();
        try {

            properties.setProperty(accessKeyPropName, credentials.getAWSAccessKeyId());
            properties.setProperty(secretKeyPropName, credentials.getAWSSecretKey());
            properties.setProperty(AMAZON_AWS_REGION, Regions.getCurrentRegion().getName());
            properties.setProperty(SUNGARGAS_WORKER_CONFIGURATION, getInstanceId());
            properties.setProperty(AMAZON_S3_BUCKET, initConfigurationDto.getS3().getBucketName());
            properties.setProperty(AMAZON_SDFS_SIZE, initConfigurationDto.getSdfs().getVolumeSize());

            properties.store(new FileOutputStream(file), "AWS Credentials");
        } catch (IOException ioException) {
            throw new ConfigurationException("Can not create amazon.properties file\n" +
                    "Check path or permission: " + file.getAbsolutePath(), ioException);
        }

    }

    @Override
    public boolean areCredentialsValid() {
        AmazonEC2Client ec2Client = new AmazonEC2Client(credentials);
        try {
            ec2Client.describeRegions();
            return true;
        } catch (AmazonClientException e) {
            LOG.warn("Provided AWS credentials are invalid.");
            return false;
        } catch (ConfigurationException credentialsNotProvided) {
            LOG.error(credentialsNotProvided.getMessage(), credentialsNotProvided);
            LOG.error("Set AWS Credentials before use areStoredCredentialsValid method");
            throw credentialsNotProvided;
        }
    }

    @Override
    public boolean isAwsPropertyFileExists() {
        return getPropertyFile().exists();
    }

    @Override
    public AWSCredentials getCredentials() {
        return credentials;
    }

    @Override
    public boolean checkDefaultUser(String login, String password) {
        return DEFAULT_LOGIN.equals(login.toLowerCase()) && password.equals(instanceId);
    }

    @Override
    public InitConfigurationDto getInitConfigurationDto() {
        initConfigurationDto = new InitConfigurationDto();
        initConfigurationDto.setDb(new InitConfigurationDto.DB());
        initConfigurationDto.getDb().setValid(false);
        initConfigurationDto.getDb().setValid(isDbExists());

        String bucketName =  "com.sungardas.snapdirector." + instanceId;
        InitConfigurationDto.S3 s3 = new InitConfigurationDto.S3();
        s3.setBucketName(bucketName);
        s3.setCreated(bucketAlreadyExists(bucketName));

        String queueName = getAccountId() + "/snapdirector_" + instanceId;
        InitConfigurationDto.Queue queue = new InitConfigurationDto.Queue();
        queue.setQueueName(queueName);
        queue.setCreated(queueAlreadyExists(queueName));

        String volumeName = "awspool";
        String mountPoint = "/mnt/awspool/";
        InitConfigurationDto.SDFS sdfs = new InitConfigurationDto.SDFS();
        sdfs.setMountPoint(mountPoint);
        sdfs.setVolumeName(volumeName);
        sdfs.setVolumeSize(Integer.toString(getSdfsVolumeMaxAvailableSizeInGB()) + "GB");
        sdfs.setCreated(sdfsAlreadyExists(volumeName, mountPoint));

        initConfigurationDto.setS3(s3);
        initConfigurationDto.setQueue(queue);
        initConfigurationDto.setSdfs(sdfs);
        return initConfigurationDto;
    }

    private boolean isDbExists() {
        String[] tables = {"BackupList", "Configurations", "Tasks", "Users", "Retention", "Snapshots"};
        AmazonDynamoDBClient amazonDynamoDB = new AmazonDynamoDBClient(credentials);
        amazonDynamoDB.setRegion(Regions.getCurrentRegion());
        try {
            ListTablesResult listResult = amazonDynamoDB.listTables();
            List<String> tableNames = listResult.getTableNames();
            LOG.info("List db structure: {}", tableNames.toArray());
            LOG.info("Check db structure is present: {}", tableNames.containsAll(Arrays.asList(tables)));
            return tableNames.containsAll(Arrays.asList(tables));
        }catch (AmazonServiceException accessError) {
            LOG.info("Can't get a list of existed tables. Check AWS credentials!", accessError);
            throw new DataAccessException(accessError);
        }
    }

    private boolean isDbValidOrAbsent() {
        String[] tables = {"BackupList", "Configurations", "Tasks", "Users", "Retention", "Snapshots"};
        AmazonDynamoDBClient amazonDynamoDB = new AmazonDynamoDBClient(credentials);
        amazonDynamoDB.setRegion(Regions.getCurrentRegion());
        try {
            ListTablesResult listResult = amazonDynamoDB.listTables();
            List<String> tableNames = listResult.getTableNames();
            return containsAllOrAny(tables, tableNames);
        }catch (AmazonServiceException accessError) {
            LOG.info("Can't get a list of existed tables. Check AWS credentials!", accessError);
            throw new DataAccessException(accessError);
        }
    }

    private boolean containsAllOrAny(String[] toCheck, List<String> where) {
        if(where.size()==0) return true;
        int expectedCount = toCheck.length;
        int actualCount = 0;
        for(String value: toCheck) {
            if(where.contains(value)) actualCount++;
        }
        return expectedCount==actualCount;
    }

    private boolean bucketAlreadyExists(String bucketName) {
        AmazonS3Client amazonS3Client = new AmazonS3Client(credentials);
        amazonS3Client.setRegion(Regions.getCurrentRegion());
        try {
            for(Bucket b: amazonS3Client.listBuckets()) {
                if (b.getName().contains(bucketName)) return true;
            }

        return amazonS3Client.listBuckets().contains(bucketName);
        }catch (AmazonServiceException accessError) {
            LOG.info("Can't get a list of S3 buckets. Check AWS credentials!", accessError);
            throw new DataAccessException(accessError);
        }
    }

    private boolean queueAlreadyExists(String queueName) {
        AmazonSQSClient amazonSQSClient = new AmazonSQSClient(credentials);
        amazonSQSClient.setRegion(Regions.getCurrentRegion());
        try {
            for(String s: amazonSQSClient.listQueues().getQueueUrls()) {
                if (s.contains(queueName)) return true;
            }
            return false;
        }catch (AmazonServiceException accessError) {
            LOG.info("Can't get a list of queues. Check AWS credentials!", accessError);
            throw new DataAccessException(accessError);
        }
    }

    private boolean sdfsAlreadyExists(String volumeName, String mountPoint) {
        String volumeConfigPath = "/etc/sdfs/" + volumeName + "-volume-cfg.xml";
        File configf = new File(volumeConfigPath);
        File mountPointf = new File(mountPoint);
        return configf.exists() && mountPointf.exists();
    }

    private int getSdfsVolumeMaxAvailableSizeInGB() {
        UnixOperatingSystemMXBean osBean= (UnixOperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

        long freeMem = osBean.getFreePhysicalMemorySize();
        LOG.info("Memory available: {}", freeMem);
        LOG.info("Default chunk size: {}", defaultChunkSize);
        long maxVolumeSize = (freeMem*defaultChunkSize)/33;
        LOG.info("Maximum volume size can be set: {}", maxVolumeSize);
        return (int)(maxVolumeSize/bytesInGB);
    }

    private void validateCredentials(String accessKey, String secretKey) {
        if(accessKey==null || accessKey.isEmpty()) {
            throw new ConfigurationException("Null or empty AWS AccessKey");
        }
        if(secretKey==null || secretKey.isEmpty()) {
            throw new ConfigurationException("Null or empty AWS SecretKey");
        }
    }

    private File getPropertyFile() {
        return Paths.get(System.getProperty(catalinaHomeEnvPropName), confFolderName, propFileName).toFile();
    }

    private String getAccountId() {
        AmazonIdentityManagementClient iamClient = new AmazonIdentityManagementClient(credentials);
        try {
            return iamClient.getUser().getUser().getArn().replaceAll("[^\\d]", "");
        }catch (AmazonServiceException accessError) {
            LOG.info("Can't get userId. Check AWS credentials!", accessError);
            throw new DataAccessException(accessError);
        }
    }

    private String getInstanceId() {
        String instanceId = null;
        try {
            URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
            URLConnection conn = url.openConnection();
            Scanner s = new Scanner(conn.getInputStream());
            if (s.hasNext()) {
                instanceId = s.next();
                LOG.info("Getting configuration id from metadata: " + instanceId);
            }
            s.close();
        } catch (IOException e) {
            LOG.warn("Failed to determine ec2 instance ID");
            throw new SnapdirectorException("Failed to determine ec2 instance ID", e);
        }
        return instanceId;
    }
}