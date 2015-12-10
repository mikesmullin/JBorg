package com.sdd.jborg.cloud;

import static com.sdd.jborg.scripts.Standard.*;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.sdd.jborg.util.Logger;

import java.util.Arrays;

public class Aws
	implements CloudDriver
{
	private String region;
	private String imageId;
	private String instanceType;
	private String keyName;
	private String subnetId;
	private String securityGroupIds;

	public String getRegion()
	{
		return region;
	}

	public Aws setRegion(final String region)
	{
		this.region = region;
		return this;
	}

	public String getImageId()
	{
		return imageId;
	}

	public Aws setImageId(final String imageId)
	{
		this.imageId = imageId;
		return this;
	}

	public String getInstanceType()
	{
		return instanceType;
	}

	public Aws setInstanceType(final String instanceType)
	{
		this.instanceType = instanceType;
		return this;
	}

	public String getSubnetId()
	{
		return subnetId;
	}

	public Aws setSubnetId(final String subnetId)
	{
		this.subnetId = subnetId;
		return this;
	}

	public String getSecurityGroupIds()
	{
		return securityGroupIds;
	}

	public Aws setSecurityGroupIds(final String securityGroupIds)
	{
		this.securityGroupIds = securityGroupIds;
		return this;
	}

	@Override
	public String getKeyName()
	{
		return keyName;
	}

	public Aws setKeyName(final String keyName)
	{
		this.keyName = keyName;
		return this;
	}

	/**
	 * Create a new instance at the cloud provider.
	 */
	public void createVirtualMachine()
	{
		final AmazonEC2Client ec2Client = new AmazonEC2Client(new EnvironmentVariableCredentialsProvider());
		ec2Client.setEndpoint("ec2." + region + ".amazonaws.com");

		// TODO: create any EBS volumes required, first

		final RunInstancesResult runInstancesResult =
			ec2Client.runInstances(new RunInstancesRequest()
				.withImageId(imageId)
				.withInstanceType(instanceType)
				.withMinCount(1)
				.withMaxCount(1)
				.withKeyName(keyName)
				//.withSubnetId(subnetId)
				//.withSecurityGroupIds(securityGroupIds)
				.withNetworkInterfaces(new InstanceNetworkInterfaceSpecification()
					.withDeviceIndex(0)
					.withSubnetId(subnetId)
					.withGroups(securityGroupIds)
					.withAssociatePublicIpAddress(true)))
				// TODO: support defining AvailabilityZone
				// TODO: support defining Tenancy (maybe not; we'll always be default)
				// TODO: support defining Elastic IP
			;

		final String instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
		final String publicIpAddress;
//		final String publicDnsName;
//		final String privateIpAddress;

		// wait until instance state is "running"...
		waiting:
		while (true)
		{
			delay(6 * 1_000, "for instance state \"running\"");

			final DescribeInstancesResult describeInstancesResult =
				ec2Client.describeInstances(new DescribeInstancesRequest()
					.withInstanceIds(instanceId));

			final Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);

			switch (instance.getState().getName())
			{
				case "pending":
					// continue waiting
					break;
				case "running":
					publicIpAddress = instance.getPublicIpAddress();
	//				publicDnsName = instance.getPublicDnsName();
	//				privateIpAddress = instance.getPrivateIpAddress();
					break waiting;
				case "shutting-down":
				case "stopping":
				case "stopped":
				case "terminated":
				default:
					die("something unexpected has happened to the instance; cannot continue.");
					return;
			}
		}

		// set machine name tag
		ec2Client.createTags(new CreateTagsRequest()
			.withResources(Arrays.asList(instanceId))
			.withTags(Arrays.asList(new Tag("Name", server.fqdn.toString()))));

		// TODO: attach any EBS volumes

		server.ssh.host = publicIpAddress;
		server.ssh.port = 22;
		server.ssh.user = "ubuntu";
		server.ssh.key = keyName;

		delay(60 * 1_000, "for instance to accept SSH connections");
		Logger.info("done creating new AWS instance "+ instanceId +".");
	}

	// TODO: support instance termination
}