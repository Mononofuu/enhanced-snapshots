# Enhanced Snapshots

![Enhanced Snapshots Logo](https://cloud.githubusercontent.com/assets/1557544/10324458/c19466ca-6c57-11e5-8318-a2eb1cd9e99b.png)

*Table of contents*
* [Product Description](#product-description)
* [Key Features](#key-features)
* [Limitations](#limitations)
* [Quick start](#quick-start)
* [Getting Started](#getting-started)
* [Management Tasks](#management-tasks)
* [Removing the Enhanced Snapshots system](#removing-the-enhanced-snapshots-system)
* [IAM user creation (optional)](#iam-user-creation-optional)
* [License](#license)

# Product Description
The Sungard AS Enhanced Snapshots software manages backups for servers located in the Amazon Web Services cloud. The product will be useful for customers who want to reduce:
* the cost of storing backups
* time spent by IT engineers on routine backup management tasks.

Using an intuitive interface, you can easily automate routine tasks such as creation of backups and deletion of old backups. Since these tasks are automated, you will minimize the risks that are associated with human error.

Technical support is not available for the first version of the product; however, please create a Github issue if you have any comments or suggestions. Customer support service may be added in a future release.

Open source is another important feature of this solution and we plan to create a community that will support it. For the end user, Enhanced Snapshots will be free.

This tool will be available by launching the AMI by creating a role like es-admin-role using the cloud formation template mentioned in the [Quick start](https://github.com/sungardas/enhanced-snapshots#quick-start) section below. Similarly the EC2 instance created and the associated resources can be removed by using the "Uninstall" button under for the Settings tab, however this will not remove the customer from the AWS subscription for the SungardAS provided marketplace products. Refer to the [Removing the enhanced snapshots system](https://github.com/sungardas/enhanced-snapshots#removing-the-enhanced-snapshots-system) for more details.

# Key Features
## Backup & Recovery 
* Performing backups of EBS volumes.
* The ability to perform recovery from historical backups.
* The ability to store backups of deleted volumes.
* Ability to quickly initiate recovery of backups.

## Schedule Policy
* Allows user to create scheduled tasks by minute, hour, day, week and month.
* Full support of CRON expressions.

## Retention Policy
* Allows user to automatically delete older backups based on the original volume’s size, count or age.
* A user can cancel any task before it is moved to the running state. 

## Management
* The ability to assign different (two) roles for users.
* Simple and intuitive wizard for initial setup process.

# Limitations
* No support for management of volumes using OS level RAID
* EBS volumes using EBS encryption must be [pre-warmed](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-prewarm.html) to avoid significant storage overhead

# Quick start
For the single AMI option from the market place Create a es-admin role using the cloud formation template as prerequisite [es-admin-role](https://github.com/SungardAS/enhanced-snapshots/blob/master/sungardas_es_admin_role_perm_restricted.template) 
 To launch a CloudFormation stack based on the template, first decide which region you will deploy in. In that region you will need the following information:

* An [EC2 keypair](https://us-east-1.console.aws.amazon.com/ec2/v2/home?#KeyPairs)
* A [VPC id](https://console.aws.amazon.com/vpc/home?#vpcs:) (Not needed if you are using the Simple Stack option below.)
* An IP prefix from which inbound http/https/ssh connections will be allowed. If you're not sure, use your current [public IP address](http://www.myipaddress.com/show-my-ip-address/) with "/32" tacked on the end (like "1.2.3.4/32").

**Note** When creating the CloudFormation stack, on the Review page, make sure to check the box at the bottom of the page in the Capabilities section.

![Capabilities_checkbox](https://cloud.githubusercontent.com/assets/1557544/10256747/2feb29cc-6921-11e5-9d3b-d7974fb5753f.png)

Once you have collected that information, find your target region, note the AMI ID for that region, and click the corresponding Launch Stack link.

| Region         | AMI ID        | Simple Stack (default VPC) | VPC Stack (your VPC) |
| -------------- | ------------- | ------------ |---------- |
| us-east-1      | ami-fd838b97  | [![Launch 
Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-east-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-east-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| us-west-1      | ami-b0b5c8d0  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-west-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-west-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-west-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-west-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| us-west-2      | ami-e0fc1680  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-west-2#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-west-2.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-west-2#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.us-west-2.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| eu-west-1      | ami-75a32506  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.eu-west-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.eu-west-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| sa-east-1      | ami-0a2fa366  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=sa-east-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.sa-east-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=sa-east-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.sa-east-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| eu-central-1      | ami-7148ae1e  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=eu-central-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.eu-central-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=eu-central-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.eu-central-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| ap-southeast-1 | ami-0c2ce66f  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-southeast-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-southeast-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-southeast-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-southeast-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| ap-southeast-2      | ami-ff01229c  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-southeast-2#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-southeast-2.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-southeast-2#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-southeast-2.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|
| ap-norhteast-1      | ami-e7aabd89  | [![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-northeast-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-northeast-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_default_vpc.template.json)|[![Launch Stack](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=ap-northeast-1#/stacks/new?stackName=enhanced-snapshots&templateURL=http://particles-enhanced-snapshots.ap-northeast-1.s3.amazonaws.com/enhanced-mktplace/particles/cftemplates/sungardas_enhanced_snapshots_with_vpc.template.json)|

Once the CloudFormation stack has finished building, go to its Outputs tab at the bottom of the AWS Console. Copy the instance ID (you will need it in a later step) and click the URL, then proceed to [Getting Started](#getting-started).

User also should to:
* Select minimum m3.large instance and minimum 8 GB size for volume
* Select es_admin role while launching instance to get acsess to CloudWatch

![Role](https://cloud.githubusercontent.com/assets/13747035/11899135/bcaaee2c-a5a5-11e5-965b-78d60d64f3b8.png)

# Getting Started
**Note** If you have not followed the [Quick start](#quick-start) section above, then you will first need to manually [create an IAM user](#iam-user-creation-optional) and then create an EC2 instance using the Enhanced Snapshots AMI, which can be found in the first table above.

**Note** By default a new instance has a self-signed SSL certificate, so you will need to bypass your browser's security warning to start.

*Step 1*

For the first login please use the following credentials:
* Login: admin@enhancedsnapshots
* Password:  Your AWS EC2 Instance ID (available on the [AWS console](https://console.aws.amazon.com/ec2))
![Login](https://cloud.githubusercontent.com/assets/14750068/10096550/1bbf5294-637b-11e5-93d2-e1de26060c46.png)

*Step 2*

The next picture shows the list of additional resources that will be created: S3 bucket, SDFS settings and DynamoDB tables. To view more information these resource the user can click the question mark.
![Settings(2)](https://cloud.githubusercontent.com/assets/13747035/11899145/ca4ea3a2-a5a5-11e5-9bbc-260e8b9e8bf3.png)

**Warning**
Once "Setup" has been clicked on this screen, these settings cannot be changed.

*Step 3*

Time to create a first user. The first user always will receive admin rights. Email is used as user ID.
![New user](https://cloud.githubusercontent.com/assets/13747035/11899153/cee6c6ec-a5a5-11e5-8cc4-fb78a04959e4.png)

*Step 4*

After the system is received all necessary input data it will create all necessary environment.
![Please wait](https://cloud.githubusercontent.com/assets/13747035/11899154/d4fe6116-a5a5-11e5-8ef5-0c5bdeaba406.png)

After the configurations process will be successfully completed, the following notification will appear.
![Congratulations](https://cloud.githubusercontent.com/assets/13747035/11899155/d9998728-a5a5-11e5-850e-ef04af12fc6a.png)

*Step 5*

The system automatically redirects the user to the login page. Now the user will use their credentials that were created in step 4.

![Login](https://cloud.githubusercontent.com/assets/14750068/10096556/1bdcd81e-637b-11e5-86ff-205b3d992a13.png)

After logging in, the list of EBS volumes for the local region is displayed.
![Volumes](https://cloud.githubusercontent.com/assets/14750068/10096555/1bda2dd0-637b-11e5-86b5-87d4463e337d.png)


# Management Tasks
## Peforming Manual Backups
To perform a backup, the user selects the appropriate EBS volume and clicks the button Backup selected. Also, the user can configure multi-backup, or several volumes during one task. For this, a user selects several appropriate volumes and clicks the button Backup selected. (Backups are performed one at a time.)
![Volumes (backup selected)](https://cloud.githubusercontent.com/assets/14750068/10023447/e410e4d8-615a-11e5-8bc3-93b9b9580528.png)

## Creating a Schedule
The user can automate the process of creating backups thanks to the Schedule feature. Schedules can be edited with Enhanced Snapshots web UI; schedules are displayed and stored in [Cron](https://en.wikipedia.org/wiki/Cron) format. The interval of backups is from one minute to one year. If necessary, schedules can be disabled.

![New schedule](https://cloud.githubusercontent.com/assets/14750068/10096546/1b936620-637b-11e5-859c-6e4e1b58d62e.png)

Users can also edit or delete existing schedules.
![Schedule menu](https://cloud.githubusercontent.com/assets/1557544/10000004/8c13719a-6067-11e5-9775-11e166d143d1.png)

## Managing Retention Policies
The retention policy function that allows the user to automatically delete backups according certain conditions: size limit, count limit days limit.
![Edit retention rule](https://cloud.githubusercontent.com/assets/1557544/9996404/c7337b3e-6054-11e5-8e12-5a0c7e139134.png)

Only one retention policy can be created for each volume. If a volume does not have any backups, the retention policy cannot be created.

Filters for users are available in order to sort according to different parameters: Volume ID, Name, Size, Instance ID and date of creation.
![Filter](https://cloud.githubusercontent.com/assets/13747035/11899172/ee749502-a5a5-11e5-829c-ec2a3721db73.png)

##  Other Management Tasks
A list of all active and pending tasks can be found in the tab Tasks. The user can cancel any task before it starts running.

A list of all users is available in the Users tab. All information about users except their passwords is displayed. Users with admin rights can make changes to all users (even other administrators). A user without administrative rights can edit only their user profile. If there is only one administrator user, administrator rights cannot be revoked.

# Removing the Enhanced Snapshots system
If you choose to remove Enhanced Snapshots, you can do so by clicking the Uninstall button on the Settings tab.
![Settings](https://cloud.githubusercontent.com/assets/13747035/11899175/f3247c98-a5a5-11e5-8883-276a8c4b999a.png)

The system will continue with the removal of all resources once you enter the EC2 Instance Id for the EC2 instance that Enhanced Snapshots is running on. 

![Delete](https://cloud.githubusercontent.com/assets/13747035/11899177/f7ebe626-a5a5-11e5-8a22-969ee55133f8.png)

The following resources are deleted:
* EC2 Instance 
* S3 bucket and all backup data
* DynamoDB tables

**Note** Though the EC2 instance will be deleted it will not remove the subscription to the software product from AWS and for more
  details on this refer to https://aws.amazon.com/marketplace/help/200799470 
  
**Note** It may take several minutes to delete all the resources, especially if backup data has been stored.

# IAM user creation (optional)
If you are creating an instance from the AMI directly without using the provided CloudFormation template, you must first create an IAM user with the following policy.
```
 {
  "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "1",
            "Effect": "Allow",
            "Action": [
                "iam:ListUserPolicies",
                "iam:GetUser",
                "iam:GetRole",
                "iam:ListRoles",
                "iam:PassRole",
                "iam:ListInstanceProfiles",
                "ec2:AttachVolume",
                "ec2:CreateSnapshot",
                "ec2:CreateTags",
                "ec2:CreateVolume",
                "ec2:DeleteSnapshot",
                "ec2:DeleteTags",
                "ec2:DeleteVolume",
                "ec2:DescribeAvailabilityZones",
                "ec2:DescribeInstanceAttribute",
                "ec2:DescribeInstanceStatus",
                "ec2:DescribeInstances",
                "ec2:TerminateInstances",
                "ec2:DescribeRegions",
                "ec2:DescribeReservedInstances",
                "ec2:DescribeReservedInstancesListings",
                "ec2:DescribeSnapshotAttribute",
                "ec2:DescribeSnapshots",
                "ec2:DescribeTags",
                "ec2:DescribeVolumeAttribute",
                "ec2:DescribeVolumeStatus",
                "ec2:DescribeVolumes",
                "ec2:DetachVolume",
                "ec2:EnableVolumeIO",
                "ec2:ModifyInstanceAttribute",
                "ec2:ModifySnapshotAttribute",
                "ec2:ModifyVolumeAttribute",
                "ec2:ResetSnapshotAttribute",
                "sqs(kiss)",
                "s3:*",
                "dynamodb(kiss)",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "logs:DescribeLogStreams"
            ],
            "Resource": "*"
        }
    ]
}
```
Once the user is created, also create and save an API key, which will be needed to configure Enhanced Snapshots. 

Without a properly configured user, the following error message will appear during configuration:
![DynamoDBAccessDenied](https://cloud.githubusercontent.com/assets/14750068/10131876/08b816c8-65dc-11e5-871e-0f8d5fcdd303.png)

# Logging

Application uses AWS CloudWatch as logs storage. Logs can be found in following location:
![Logs](https://cloud.githubusercontent.com/assets/13747035/11899181/fc56c14a-a5a5-11e5-9b26-c764b65bdbd6.png)

# License

See the [LICENSE.md](LICENSE.md) file for license rights and limitations (Apache 2).

Use of the provided AMIs is covered by a separate [End User License Agreement](https://s3-us-west-2.amazonaws.com/sgaslogo/EULA_Enhanced+Snapshots+for+AWS+2015-10-27.docx).

## [![Sungard Availability Services | Labs][labs-logo]][labs-github-url]

This project is maintained by the Labs group at [Sungard Availability
Services](http://sungardas.com)

GitHub: [https://sungardas.github.io](https://sungardas.github.io)

Blog:
[http://blog.sungardas.com/CTOLabs/](http://blog.sungardas.com/CTOLabs/)


[labs-github-url]: https://sungardas.github.io
[labs-logo]: https://raw.githubusercontent.com/SungardAS/repo-assets/master/images/logos/sungardas-labs-logo-small.png

