/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.services.s3.model.transform;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.internal.DeleteObjectsResponse;
import com.amazonaws.services.s3.internal.ObjectExpirationResult;
import com.amazonaws.services.s3.internal.S3RequesterChargedResult;
import com.amazonaws.services.s3.internal.S3VersionResult;
import com.amazonaws.services.s3.internal.ServerSideEncryptionResult;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AccessControlTranslation;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketAccelerateConfiguration;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.NoncurrentVersionTransition;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Rule;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Transition;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketReplicationConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.CORSRule.AllowedMethods;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectsResult.DeletedObject;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.EncryptionConfiguration;
import com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.Grantee;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PartSummary;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.RedirectRule;
import com.amazonaws.services.s3.model.ReplicationDestinationConfig;
import com.amazonaws.services.s3.model.ReplicationRule;
import com.amazonaws.services.s3.model.RequestPaymentConfiguration;
import com.amazonaws.services.s3.model.RequestPaymentConfiguration.Payer;
import com.amazonaws.services.s3.model.RoutingRule;
import com.amazonaws.services.s3.model.RoutingRuleCondition;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.SourceSelectionCriteria;
import com.amazonaws.services.s3.model.SseKmsEncryptedObjects;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.TagSet;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.model.analytics.AnalyticsAndOperator;
import com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration;
import com.amazonaws.services.s3.model.analytics.AnalyticsExportDestination;
import com.amazonaws.services.s3.model.analytics.AnalyticsFilter;
import com.amazonaws.services.s3.model.analytics.AnalyticsFilterPredicate;
import com.amazonaws.services.s3.model.analytics.AnalyticsPrefixPredicate;
import com.amazonaws.services.s3.model.analytics.AnalyticsS3BucketDestination;
import com.amazonaws.services.s3.model.analytics.AnalyticsTagPredicate;
import com.amazonaws.services.s3.model.analytics.StorageClassAnalysis;
import com.amazonaws.services.s3.model.analytics.StorageClassAnalysisDataExport;
import com.amazonaws.services.s3.model.inventory.InventoryConfiguration;
import com.amazonaws.services.s3.model.inventory.InventoryDestination;
import com.amazonaws.services.s3.model.inventory.InventoryFilter;
import com.amazonaws.services.s3.model.inventory.InventoryPrefixPredicate;
import com.amazonaws.services.s3.model.inventory.InventoryS3BucketDestination;
import com.amazonaws.services.s3.model.inventory.InventorySchedule;
import com.amazonaws.services.s3.model.inventory.ServerSideEncryptionKMS;
import com.amazonaws.services.s3.model.inventory.ServerSideEncryptionS3;
import com.amazonaws.services.s3.model.lifecycle.LifecycleAndOperator;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilterPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;
import com.amazonaws.services.s3.model.metrics.MetricsAndOperator;
import com.amazonaws.services.s3.model.metrics.MetricsConfiguration;
import com.amazonaws.services.s3.model.metrics.MetricsFilter;
import com.amazonaws.services.s3.model.metrics.MetricsFilterPredicate;
import com.amazonaws.services.s3.model.metrics.MetricsPrefixPredicate;
import com.amazonaws.services.s3.model.metrics.MetricsTagPredicate;
import com.amazonaws.util.DateUtils;
import com.amazonaws.util.SdkHttpUtils;
import com.amazonaws.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.amazonaws.util.StringUtils.UTF8;

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
//
//  This class is copied from aws-java-sdk-s3 (Apache License 2.0)
//  in order to replaced {@code XMLReaderFactory.createXMLReader} by
//  {@code SAX_PARSER_FACTORY.newSAXParser().getXMLReader()} to
//  avoid JDK-8015099.
//
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
 * XML Sax parser to read XML documents returned by S3 via the REST interface, converting these
 * documents into objects.
 */
public class XmlResponsesSaxParser {
    private static final Log log = LogFactory.getLog(XmlResponsesSaxParser.class);

    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();

    private XMLReader xr = null;

    private boolean sanitizeXmlDocument = true;

    /**
     * Constructs the XML SAX parser.
     *
     * @throws SdkClientException
     */
    public XmlResponsesSaxParser() throws SdkClientException {
        // Ensure we can load the XML Reader.
        try {
            // Need to explicitly enable Sax' NAMESPACES_FEATURE (default true) in JAXP (default
            // false)!
            SAX_PARSER_FACTORY.setNamespaceAware(true);
            xr = SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
            disableExternalResourceFetching(xr);
        } catch (SAXException | ParserConfigurationException e) {
            throw new SdkClientException(
                    "Couldn't initialize a SAX driver to create an XMLReader", e);
        }
    }

    /**
     * Parses an XML document from an input stream using a document handler.
     *
     * @param handler the handler for the XML document
     * @param inputStream an input stream containing the XML document to parse
     * @throws IOException on error reading from the input stream (ie connection reset)
     * @throws SdkClientException on error with malformed XML, etc
     */
    protected void parseXmlInputStream(DefaultHandler handler, InputStream inputStream)
            throws IOException {
        try {

            if (log.isDebugEnabled()) {
                log.debug("Parsing XML response document with handler: " + handler.getClass());
            }

            BufferedReader breader =
                    new BufferedReader(
                            new InputStreamReader(inputStream, Constants.DEFAULT_ENCODING));
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(breader));

        } catch (IOException e) {
            throw e;

        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to close response InputStream up after XML parse failure", e);
                }
            }
            throw new SdkClientException(
                    "Failed to parse XML document with handler " + handler.getClass(), t);
        }
    }

    protected InputStream sanitizeXmlDocument(DefaultHandler handler, InputStream inputStream)
            throws IOException {

        if (!sanitizeXmlDocument) {
            // No sanitizing will be performed, return the original input stream unchanged.
            return inputStream;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Sanitizing XML document destined for handler " + handler.getClass());
            }

            InputStream sanitizedInputStream = null;

            try {

                /*
                 * Read object listing XML document from input stream provided into a
                 * string buffer, so we can replace troublesome characters before
                 * sending the document to the XML parser.
                 */
                StringBuilder listingDocBuffer = new StringBuilder();
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(inputStream, Constants.DEFAULT_ENCODING));

                char[] buf = new char[8192];
                int read = -1;
                while ((read = br.read(buf)) != -1) {
                    listingDocBuffer.append(buf, 0, read);
                }
                br.close();

                /*
                 * Replace any carriage return (\r) characters with explicit XML
                 * character entities, to prevent the SAX parser from
                 * misinterpreting 0x0D characters as 0x0A and being unable to
                 * parse the XML.
                 */
                String listingDoc = listingDocBuffer.toString().replaceAll("\r", "&#013;");

                sanitizedInputStream = new ByteArrayInputStream(listingDoc.getBytes(UTF8));

            } catch (IOException e) {
                throw e;

            } catch (Throwable t) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(
                                "Unable to close response InputStream after failure sanitizing XML document",
                                e);
                    }
                }
                throw new SdkClientException(
                        "Failed to sanitize XML document destined for handler "
                                + handler.getClass(),
                        t);
            }
            return sanitizedInputStream;
        }
    }

    /**
     * Disables certain dangerous features that attempt to automatically fetch DTDs
     *
     * <p>See <a
     * href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#XMLReader">OWASP
     * XXE Cheat Sheet</a>
     *
     * @param reader the reader to disable the features on
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    private void disableExternalResourceFetching(XMLReader reader)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    }

    /**
     * Checks if the specified string is empty or null and if so, returns null. Otherwise simply
     * returns the string.
     *
     * @param s The string to check.
     * @return Null if the specified string was null, or empty, otherwise returns the string the
     *     caller passed in.
     */
    private static String checkForEmptyString(String s) {
        if (s == null) return null;
        if (s.length() == 0) return null;

        return s;
    }

    /**
     * Safely parses the specified string as an integer and returns the value. If a
     * NumberFormatException occurs while parsing the integer, an error is logged and -1 is
     * returned.
     *
     * @param s The string to parse and return as an integer.
     * @return The integer value of the specified string, otherwise -1 if there were any problems
     *     parsing the string as an integer.
     */
    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            log.error("Unable to parse integer value '" + s + "'", nfe);
        }

        return -1;
    }

    /**
     * Safely parses the specified string as a long and returns the value. If a
     * NumberFormatException occurs while parsing the long, an error is logged and -1 is returned.
     *
     * @param s The string to parse and return as a long.
     * @return The long value of the specified string, otherwise -1 if there were any problems
     *     parsing the string as a long.
     */
    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException nfe) {
            log.error("Unable to parse long value '" + s + "'", nfe);
        }

        return -1;
    }

    /** Perform a url decode on the given value if specified. Return value by default; */
    private static String decodeIfSpecified(String value, boolean decode) {
        return decode ? SdkHttpUtils.urlDecode(value) : value;
    }

    /**
     * Parses a ListBucket response XML document from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public ListBucketHandler parseListBucketObjectsResponse(
            InputStream inputStream, final boolean shouldSDKDecodeResponse) throws IOException {
        ListBucketHandler handler = new ListBucketHandler(shouldSDKDecodeResponse);
        parseXmlInputStream(handler, sanitizeXmlDocument(handler, inputStream));

        return handler;
    }

    /**
     * Parses a ListBucketV2 response XML document from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public ListObjectsV2Handler parseListObjectsV2Response(
            InputStream inputStream, final boolean shouldSDKDecodeResponse) throws IOException {
        ListObjectsV2Handler handler = new ListObjectsV2Handler(shouldSDKDecodeResponse);
        parseXmlInputStream(handler, sanitizeXmlDocument(handler, inputStream));

        return handler;
    }

    /**
     * Parses a ListVersions response XML document from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public ListVersionsHandler parseListVersionsResponse(
            InputStream inputStream, final boolean shouldSDKDecodeResponse) throws IOException {
        ListVersionsHandler handler = new ListVersionsHandler(shouldSDKDecodeResponse);
        parseXmlInputStream(handler, sanitizeXmlDocument(handler, inputStream));
        return handler;
    }

    /**
     * Parses a ListAllMyBuckets response XML document from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public ListAllMyBucketsHandler parseListMyBucketsResponse(InputStream inputStream)
            throws IOException {
        ListAllMyBucketsHandler handler = new ListAllMyBucketsHandler();
        parseXmlInputStream(handler, sanitizeXmlDocument(handler, inputStream));
        return handler;
    }

    /**
     * Parses an AccessControlListHandler response XML document from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public AccessControlListHandler parseAccessControlListResponse(InputStream inputStream)
            throws IOException {
        AccessControlListHandler handler = new AccessControlListHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    /**
     * Parses a LoggingStatus response XML document for a bucket from an input stream.
     *
     * @param inputStream XML data input stream.
     * @return the XML handler object populated with data parsed from the XML stream.
     * @throws SdkClientException
     */
    public BucketLoggingConfigurationHandler parseLoggingStatusResponse(InputStream inputStream)
            throws IOException {
        BucketLoggingConfigurationHandler handler = new BucketLoggingConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketLifecycleConfigurationHandler parseBucketLifecycleConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketLifecycleConfigurationHandler handler = new BucketLifecycleConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketCrossOriginConfigurationHandler parseBucketCrossOriginConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketCrossOriginConfigurationHandler handler = new BucketCrossOriginConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public String parseBucketLocationResponse(InputStream inputStream) throws IOException {
        BucketLocationHandler handler = new BucketLocationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler.getLocation();
    }

    public BucketVersioningConfigurationHandler parseVersioningConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketVersioningConfigurationHandler handler = new BucketVersioningConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketWebsiteConfigurationHandler parseWebsiteConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketWebsiteConfigurationHandler handler = new BucketWebsiteConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketReplicationConfigurationHandler parseReplicationConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketReplicationConfigurationHandler handler = new BucketReplicationConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketTaggingConfigurationHandler parseTaggingConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketTaggingConfigurationHandler handler = new BucketTaggingConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public BucketAccelerateConfigurationHandler parseAccelerateConfigurationResponse(
            InputStream inputStream) throws IOException {
        BucketAccelerateConfigurationHandler handler = new BucketAccelerateConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public DeleteObjectsHandler parseDeletedObjectsResult(InputStream inputStream)
            throws IOException {
        DeleteObjectsHandler handler = new DeleteObjectsHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public CopyObjectResultHandler parseCopyObjectResponse(InputStream inputStream)
            throws IOException {
        CopyObjectResultHandler handler = new CopyObjectResultHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public CompleteMultipartUploadHandler parseCompleteMultipartUploadResponse(
            InputStream inputStream) throws IOException {
        CompleteMultipartUploadHandler handler = new CompleteMultipartUploadHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public InitiateMultipartUploadHandler parseInitiateMultipartUploadResponse(
            InputStream inputStream) throws IOException {
        InitiateMultipartUploadHandler handler = new InitiateMultipartUploadHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public ListMultipartUploadsHandler parseListMultipartUploadsResponse(InputStream inputStream)
            throws IOException {
        ListMultipartUploadsHandler handler = new ListMultipartUploadsHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public ListPartsHandler parseListPartsResponse(InputStream inputStream) throws IOException {
        ListPartsHandler handler = new ListPartsHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public GetObjectTaggingHandler parseObjectTaggingResponse(InputStream inputStream)
            throws IOException {
        GetObjectTaggingHandler handler = new GetObjectTaggingHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public GetBucketMetricsConfigurationHandler parseGetBucketMetricsConfigurationResponse(
            InputStream inputStream) throws IOException {
        GetBucketMetricsConfigurationHandler handler = new GetBucketMetricsConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public ListBucketMetricsConfigurationsHandler parseListBucketMetricsConfigurationsResponse(
            InputStream inputStream) throws IOException {
        ListBucketMetricsConfigurationsHandler handler =
                new ListBucketMetricsConfigurationsHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public GetBucketAnalyticsConfigurationHandler parseGetBucketAnalyticsConfigurationResponse(
            InputStream inputStream) throws IOException {
        GetBucketAnalyticsConfigurationHandler handler =
                new GetBucketAnalyticsConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public ListBucketAnalyticsConfigurationHandler parseListBucketAnalyticsConfigurationResponse(
            InputStream inputStream) throws IOException {
        ListBucketAnalyticsConfigurationHandler handler =
                new ListBucketAnalyticsConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public GetBucketInventoryConfigurationHandler parseGetBucketInventoryConfigurationResponse(
            InputStream inputStream) throws IOException {
        GetBucketInventoryConfigurationHandler handler =
                new GetBucketInventoryConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public ListBucketInventoryConfigurationsHandler parseBucketListInventoryConfigurationsResponse(
            InputStream inputStream) throws IOException {
        ListBucketInventoryConfigurationsHandler handler =
                new ListBucketInventoryConfigurationsHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    /**
     * @param inputStream
     * @return true if the bucket's is configured as Requester Pays, false if it is configured as
     *     Owner pays.
     * @throws SdkClientException
     */
    public RequestPaymentConfigurationHandler parseRequestPaymentConfigurationResponse(
            InputStream inputStream) throws IOException {
        RequestPaymentConfigurationHandler handler = new RequestPaymentConfigurationHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    // ////////////
    // Handlers //
    // ////////////

    /** Handler for ListBucket response XML documents. */
    public static class ListBucketHandler extends AbstractHandler {
        private final ObjectListing objectListing = new ObjectListing();
        private final boolean shouldSDKDecodeResponse;

        private S3ObjectSummary currentObject = null;
        private Owner currentOwner = null;
        private String lastKey = null;

        public ListBucketHandler(final boolean shouldSDKDecodeResponse) {
            this.shouldSDKDecodeResponse = shouldSDKDecodeResponse;
        }

        public ObjectListing getObjectListing() {
            return objectListing;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListBucketResult")) {
                if ("Contents".equals(name)) {
                    currentObject = new S3ObjectSummary();
                    currentObject.setBucketName(objectListing.getBucketName());
                }
            } else if (in("ListBucketResult", "Contents")) {
                if ("Owner".equals(name)) {
                    currentOwner = new Owner();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (atTopLevel()) {
                if ("ListBucketResult".equals(name)) {
                    /*
                     * S3 only includes the NextMarker XML element if the
                     * request specified a delimiter, but for consistency we'd
                     * like to always give easy access to the next marker if
                     * we're returning a list of results that's truncated.
                     */
                    if (objectListing.isTruncated() && objectListing.getNextMarker() == null) {

                        String nextMarker = null;
                        if (!objectListing.getObjectSummaries().isEmpty()) {
                            nextMarker =
                                    objectListing
                                            .getObjectSummaries()
                                            .get(objectListing.getObjectSummaries().size() - 1)
                                            .getKey();

                        } else if (!objectListing.getCommonPrefixes().isEmpty()) {
                            nextMarker =
                                    objectListing
                                            .getCommonPrefixes()
                                            .get(objectListing.getCommonPrefixes().size() - 1);
                        } else {
                            log.error(
                                    "S3 response indicates truncated results, "
                                            + "but contains no object summaries or "
                                            + "common prefixes.");
                        }

                        objectListing.setNextMarker(nextMarker);
                    }
                }
            } else if (in("ListBucketResult")) {
                if ("Name".equals(name)) {
                    objectListing.setBucketName(getText());
                    if (log.isDebugEnabled()) {
                        log.debug("Examining listing for bucket: " + objectListing.getBucketName());
                    }

                } else if ("Prefix".equals(name)) {
                    objectListing.setPrefix(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("Marker".equals(name)) {
                    objectListing.setMarker(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("NextMarker".equals(name)) {
                    objectListing.setNextMarker(
                            decodeIfSpecified(getText(), shouldSDKDecodeResponse));

                } else if ("MaxKeys".equals(name)) {
                    objectListing.setMaxKeys(parseInt(getText()));

                } else if ("Delimiter".equals(name)) {
                    objectListing.setDelimiter(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("EncodingType".equals(name)) {
                    objectListing.setEncodingType(
                            shouldSDKDecodeResponse ? null : checkForEmptyString(getText()));
                } else if ("IsTruncated".equals(name)) {
                    String isTruncatedStr = StringUtils.lowerCase(getText());

                    if (isTruncatedStr.startsWith("false")) {
                        objectListing.setTruncated(false);
                    } else if (isTruncatedStr.startsWith("true")) {
                        objectListing.setTruncated(true);
                    } else {
                        throw new IllegalStateException(
                                "Invalid value for IsTruncated field: " + isTruncatedStr);
                    }

                } else if ("Contents".equals(name)) {
                    objectListing.getObjectSummaries().add(currentObject);
                    currentObject = null;
                }
            } else if (in("ListBucketResult", "Contents")) {
                if ("Key".equals(name)) {
                    lastKey = getText();
                    currentObject.setKey(decodeIfSpecified(lastKey, shouldSDKDecodeResponse));
                } else if ("LastModified".equals(name)) {
                    currentObject.setLastModified(ServiceUtils.parseIso8601Date(getText()));

                } else if ("ETag".equals(name)) {
                    currentObject.setETag(ServiceUtils.removeQuotes(getText()));

                } else if ("Size".equals(name)) {
                    currentObject.setSize(parseLong(getText()));

                } else if ("StorageClass".equals(name)) {
                    currentObject.setStorageClass(getText());

                } else if ("Owner".equals(name)) {
                    currentObject.setOwner(currentOwner);
                    currentOwner = null;
                }
            } else if (in("ListBucketResult", "Contents", "Owner")) {
                if ("ID".equals(name)) {
                    currentOwner.setId(getText());

                } else if ("DisplayName".equals(name)) {
                    currentOwner.setDisplayName(getText());
                }
            } else if (in("ListBucketResult", "CommonPrefixes")) {
                if ("Prefix".equals(name)) {
                    objectListing
                            .getCommonPrefixes()
                            .add(decodeIfSpecified(getText(), shouldSDKDecodeResponse));
                }
            }
        }
    }

    /** Handler for ListObjectsV2 response XML documents. */
    public static class ListObjectsV2Handler extends AbstractHandler {
        private final ListObjectsV2Result result = new ListObjectsV2Result();
        private final boolean shouldSDKDecodeResponse;

        private S3ObjectSummary currentObject = null;
        private Owner currentOwner = null;
        private String lastKey = null;

        public ListObjectsV2Handler(final boolean shouldSDKDecodeResponse) {
            this.shouldSDKDecodeResponse = shouldSDKDecodeResponse;
        }

        public ListObjectsV2Result getResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListBucketResult")) {
                if ("Contents".equals(name)) {
                    currentObject = new S3ObjectSummary();
                    currentObject.setBucketName(result.getBucketName());
                }
            } else if (in("ListBucketResult", "Contents")) {
                if ("Owner".equals(name)) {
                    currentOwner = new Owner();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (atTopLevel()) {
                if ("ListBucketResult".equals(name)) {
                    /*
                     * S3 only includes the NextContinuationToken XML element if the
                     * request specified a delimiter, but for consistency we'd
                     * like to always give easy access to the next token if
                     * we're returning a list of results that's truncated.
                     */
                    if (result.isTruncated() && result.getNextContinuationToken() == null) {

                        String nextContinuationToken = null;
                        if (!result.getObjectSummaries().isEmpty()) {
                            nextContinuationToken =
                                    result.getObjectSummaries()
                                            .get(result.getObjectSummaries().size() - 1)
                                            .getKey();

                        } else {
                            log.error(
                                    "S3 response indicates truncated results, "
                                            + "but contains no object summaries.");
                        }

                        result.setNextContinuationToken(nextContinuationToken);
                    }
                }
            } else if (in("ListBucketResult")) {
                if ("Name".equals(name)) {
                    result.setBucketName(getText());
                    if (log.isDebugEnabled()) {
                        log.debug("Examining listing for bucket: " + result.getBucketName());
                    }

                } else if ("Prefix".equals(name)) {
                    result.setPrefix(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("MaxKeys".equals(name)) {
                    result.setMaxKeys(parseInt(getText()));

                } else if ("NextContinuationToken".equals(name)) {
                    result.setNextContinuationToken(getText());

                } else if ("ContinuationToken".equals(name)) {
                    result.setContinuationToken(getText());

                } else if ("StartAfter".equals(name)) {
                    result.setStartAfter(decodeIfSpecified(getText(), shouldSDKDecodeResponse));

                } else if ("KeyCount".equals(name)) {
                    result.setKeyCount(parseInt(getText()));

                } else if ("Delimiter".equals(name)) {
                    result.setDelimiter(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("EncodingType".equals(name)) {
                    result.setEncodingType(checkForEmptyString(getText()));
                } else if ("IsTruncated".equals(name)) {
                    String isTruncatedStr = StringUtils.lowerCase(getText());

                    if (isTruncatedStr.startsWith("false")) {
                        result.setTruncated(false);
                    } else if (isTruncatedStr.startsWith("true")) {
                        result.setTruncated(true);
                    } else {
                        throw new IllegalStateException(
                                "Invalid value for IsTruncated field: " + isTruncatedStr);
                    }

                } else if ("Contents".equals(name)) {
                    result.getObjectSummaries().add(currentObject);
                    currentObject = null;
                }
            } else if (in("ListBucketResult", "Contents")) {
                if ("Key".equals(name)) {
                    lastKey = getText();
                    currentObject.setKey(decodeIfSpecified(lastKey, shouldSDKDecodeResponse));
                } else if ("LastModified".equals(name)) {
                    currentObject.setLastModified(ServiceUtils.parseIso8601Date(getText()));

                } else if ("ETag".equals(name)) {
                    currentObject.setETag(ServiceUtils.removeQuotes(getText()));

                } else if ("Size".equals(name)) {
                    currentObject.setSize(parseLong(getText()));

                } else if ("StorageClass".equals(name)) {
                    currentObject.setStorageClass(getText());

                } else if ("Owner".equals(name)) {
                    currentObject.setOwner(currentOwner);
                    currentOwner = null;
                }
            } else if (in("ListBucketResult", "Contents", "Owner")) {
                if ("ID".equals(name)) {
                    currentOwner.setId(getText());

                } else if ("DisplayName".equals(name)) {
                    currentOwner.setDisplayName(getText());
                }
            } else if (in("ListBucketResult", "CommonPrefixes")) {
                if ("Prefix".equals(name)) {
                    result.getCommonPrefixes()
                            .add(decodeIfSpecified(getText(), shouldSDKDecodeResponse));
                }
            }
        }
    }

    /**
     * Handler for ListAllMyBuckets response XML documents. The document is parsed into {@link
     * Bucket}s available via the {@link #getBuckets()} method.
     */
    public static class ListAllMyBucketsHandler extends AbstractHandler {

        private final List<Bucket> buckets = new ArrayList<Bucket>();
        private Owner bucketsOwner = null;

        private Bucket currentBucket = null;

        /** @return the buckets listed in the document. */
        public List<Bucket> getBuckets() {
            return buckets;
        }

        /** @return the owner of the buckets. */
        public Owner getOwner() {
            return bucketsOwner;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListAllMyBucketsResult")) {
                if ("Owner".equals(name)) {
                    bucketsOwner = new Owner();
                }
            } else if (in("ListAllMyBucketsResult", "Buckets")) {
                if ("Bucket".equals(name)) {
                    currentBucket = new Bucket();
                    currentBucket.setOwner(bucketsOwner);
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("ListAllMyBucketsResult", "Owner")) {
                if ("ID".equals(name)) {
                    bucketsOwner.setId(getText());

                } else if ("DisplayName".equals(name)) {
                    bucketsOwner.setDisplayName(getText());
                }
            } else if (in("ListAllMyBucketsResult", "Buckets")) {
                if ("Bucket".equals(name)) {
                    buckets.add(currentBucket);
                    currentBucket = null;
                }
            } else if (in("ListAllMyBucketsResult", "Buckets", "Bucket")) {
                if ("Name".equals(name)) {
                    currentBucket.setName(getText());

                } else if ("CreationDate".equals(name)) {
                    Date creationDate = DateUtils.parseISO8601Date(getText());
                    currentBucket.setCreationDate(creationDate);
                }
            }
        }
    }

    /**
     * Handler for AccessControlList response XML documents. The document is parsed into an {@link
     * AccessControlList} object available via the {@link #getAccessControlList()} method.
     */
    public static class AccessControlListHandler extends AbstractHandler {

        private final AccessControlList accessControlList = new AccessControlList();

        private Grantee currentGrantee = null;
        private Permission currentPermission = null;

        /** @return an object representing the ACL document. */
        public AccessControlList getAccessControlList() {
            return accessControlList;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("AccessControlPolicy")) {
                if ("Owner".equals(name)) {
                    accessControlList.setOwner(new Owner());
                }
            } else if (in("AccessControlPolicy", "AccessControlList", "Grant")) {
                if ("Grantee".equals(name)) {
                    String type = XmlResponsesSaxParser.findAttributeValue("xsi:type", attrs);

                    if ("AmazonCustomerByEmail".equals(type)) {
                        currentGrantee = new EmailAddressGrantee(null);
                    } else if ("CanonicalUser".equals(type)) {
                        currentGrantee = new CanonicalGrantee(null);
                    } else if ("Group".equals(type)) {
                        /*
                         * Nothing to do for GroupGrantees here since we
                         * can't construct an empty enum value early.
                         */
                    }
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("AccessControlPolicy", "Owner")) {
                if ("ID".equals(name)) {
                    accessControlList.getOwner().setId(getText());
                } else if ("DisplayName".equals(name)) {
                    accessControlList.getOwner().setDisplayName(getText());
                }
            } else if (in("AccessControlPolicy", "AccessControlList")) {
                if ("Grant".equals(name)) {
                    accessControlList.grantPermission(currentGrantee, currentPermission);

                    currentGrantee = null;
                    currentPermission = null;
                }
            } else if (in("AccessControlPolicy", "AccessControlList", "Grant")) {
                if ("Permission".equals(name)) {
                    currentPermission = Permission.parsePermission(getText());
                }
            } else if (in("AccessControlPolicy", "AccessControlList", "Grant", "Grantee")) {
                if ("ID".equals(name)) {
                    currentGrantee.setIdentifier(getText());

                } else if ("EmailAddress".equals(name)) {
                    currentGrantee.setIdentifier(getText());

                } else if ("URI".equals(name)) {
                    /*
                     * Only GroupGrantees contain an URI element in them, and we
                     * can't construct currentGrantee during startElement for a
                     * GroupGrantee since it's an enum.
                     */
                    currentGrantee = GroupGrantee.parseGroupGrantee(getText());

                } else if ("DisplayName".equals(name)) {
                    ((CanonicalGrantee) currentGrantee).setDisplayName(getText());
                }
            }
        }
    }

    /**
     * Handler for LoggingStatus response XML documents for a bucket. The document is parsed into an
     * {@link BucketLoggingConfiguration} object available via the {@link
     * #getBucketLoggingConfiguration()} method.
     */
    public static class BucketLoggingConfigurationHandler extends AbstractHandler {

        private final BucketLoggingConfiguration bucketLoggingConfiguration =
                new BucketLoggingConfiguration();

        /** @return an object representing the bucket's LoggingStatus document. */
        public BucketLoggingConfiguration getBucketLoggingConfiguration() {
            return bucketLoggingConfiguration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("BucketLoggingStatus", "LoggingEnabled")) {
                if ("TargetBucket".equals(name)) {
                    bucketLoggingConfiguration.setDestinationBucketName(getText());

                } else if ("TargetPrefix".equals(name)) {
                    bucketLoggingConfiguration.setLogFilePrefix(getText());
                }
            }
        }
    }

    /**
     * Handler for CreateBucketConfiguration response XML documents for a bucket. The document is
     * parsed into a String representing the bucket's location, available via the {@link
     * #getLocation()} method.
     */
    public static class BucketLocationHandler extends AbstractHandler {

        private String location = null;

        /** @return the bucket's location. */
        public String getLocation() {
            return location;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (atTopLevel()) {
                if ("LocationConstraint".equals(name)) {
                    String elementText = getText();
                    if (elementText.length() == 0) {
                        location = null;
                    } else {
                        location = elementText;
                    }
                }
            }
        }
    }

    public static class CopyObjectResultHandler extends AbstractSSEHandler
            implements ObjectExpirationResult, S3RequesterChargedResult, S3VersionResult {

        // Data items for successful copy
        private final CopyObjectResult result = new CopyObjectResult();

        // Data items for failed copy
        private String errorCode = null;
        private String errorMessage = null;
        private String errorRequestId = null;
        private String errorHostId = null;
        private boolean receivedErrorResponse = false;

        @Override
        protected ServerSideEncryptionResult sseResult() {
            return result;
        }

        public Date getLastModified() {
            return result.getLastModifiedDate();
        }

        @Override
        public String getVersionId() {
            return result.getVersionId();
        }

        @Override
        public void setVersionId(String versionId) {
            result.setVersionId(versionId);
        }

        @Override
        public Date getExpirationTime() {
            return result.getExpirationTime();
        }

        @Override
        public void setExpirationTime(Date expirationTime) {
            result.setExpirationTime(expirationTime);
        }

        @Override
        public String getExpirationTimeRuleId() {
            return result.getExpirationTimeRuleId();
        }

        @Override
        public void setExpirationTimeRuleId(String expirationTimeRuleId) {
            result.setExpirationTimeRuleId(expirationTimeRuleId);
        }

        public String getETag() {
            return result.getETag();
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorHostId() {
            return errorHostId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorRequestId() {
            return errorRequestId;
        }

        public boolean isErrorResponse() {
            return receivedErrorResponse;
        }

        public boolean isRequesterCharged() {
            return result.isRequesterCharged();
        }

        public void setRequesterCharged(boolean isRequesterCharged) {
            result.setRequesterCharged(isRequesterCharged);
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (atTopLevel()) {
                if ("CopyObjectResult".equals(name) || "CopyPartResult".equals(name)) {
                    receivedErrorResponse = false;
                } else if ("Error".equals(name)) {
                    receivedErrorResponse = true;
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("CopyObjectResult") || in("CopyPartResult")) {
                if ("LastModified".equals(name)) {
                    result.setLastModifiedDate(ServiceUtils.parseIso8601Date(getText()));
                } else if ("ETag".equals(name)) {
                    result.setETag(ServiceUtils.removeQuotes(getText()));
                }
            } else if (in("Error")) {
                if ("Code".equals(name)) {
                    errorCode = getText();
                } else if ("Message".equals(name)) {
                    errorMessage = getText();
                } else if ("RequestId".equals(name)) {
                    errorRequestId = getText();
                } else if ("HostId".equals(name)) {
                    errorHostId = getText();
                }
            }
        }
    }

    /**
     * Handler for parsing RequestPaymentConfiguration XML response associated with an Amazon S3
     * bucket. The XML response is parsed into a <code>RequestPaymentConfiguration</code> object.
     */
    public static class RequestPaymentConfigurationHandler extends AbstractHandler {

        private String payer = null;

        public RequestPaymentConfiguration getConfiguration() {
            return new RequestPaymentConfiguration(Payer.valueOf(payer));
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("RequestPaymentConfiguration")) {
                if ("Payer".equals(name)) {
                    payer = getText();
                }
            }
        }
    }

    /** Handler for ListVersionsResult XML document. */
    public static class ListVersionsHandler extends AbstractHandler {

        private final VersionListing versionListing = new VersionListing();
        private final boolean shouldSDKDecodeResponse;

        private S3VersionSummary currentVersionSummary;
        private Owner currentOwner;

        public ListVersionsHandler(final boolean shouldSDKDecodeResponse) {
            this.shouldSDKDecodeResponse = shouldSDKDecodeResponse;
        }

        public VersionListing getListing() {
            return versionListing;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListVersionsResult")) {
                if ("Version".equals(name)) {
                    currentVersionSummary = new S3VersionSummary();
                    currentVersionSummary.setBucketName(versionListing.getBucketName());

                } else if ("DeleteMarker".equals(name)) {
                    currentVersionSummary = new S3VersionSummary();
                    currentVersionSummary.setBucketName(versionListing.getBucketName());
                    currentVersionSummary.setIsDeleteMarker(true);
                }
            } else if (in("ListVersionsResult", "Version")
                    || in("ListVersionsResult", "DeleteMarker")) {
                if ("Owner".equals(name)) {
                    currentOwner = new Owner();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {

            if (in("ListVersionsResult")) {
                if ("Name".equals(name)) {
                    versionListing.setBucketName(getText());

                } else if ("Prefix".equals(name)) {
                    versionListing.setPrefix(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));
                } else if ("KeyMarker".equals(name)) {
                    versionListing.setKeyMarker(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));
                } else if ("VersionIdMarker".equals(name)) {
                    versionListing.setVersionIdMarker(checkForEmptyString(getText()));

                } else if ("MaxKeys".equals(name)) {
                    versionListing.setMaxKeys(Integer.parseInt(getText()));

                } else if ("Delimiter".equals(name)) {
                    versionListing.setDelimiter(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("EncodingType".equals(name)) {
                    versionListing.setEncodingType(
                            shouldSDKDecodeResponse ? null : checkForEmptyString(getText()));
                } else if ("NextKeyMarker".equals(name)) {
                    versionListing.setNextKeyMarker(
                            decodeIfSpecified(
                                    checkForEmptyString(getText()), shouldSDKDecodeResponse));

                } else if ("NextVersionIdMarker".equals(name)) {
                    versionListing.setNextVersionIdMarker(getText());

                } else if ("IsTruncated".equals(name)) {
                    versionListing.setTruncated("true".equals(getText()));

                } else if ("Version".equals(name) || "DeleteMarker".equals(name)) {

                    versionListing.getVersionSummaries().add(currentVersionSummary);

                    currentVersionSummary = null;
                }
            } else if (in("ListVersionsResult", "CommonPrefixes")) {
                if ("Prefix".equals(name)) {
                    final String commonPrefix = checkForEmptyString(getText());
                    versionListing
                            .getCommonPrefixes()
                            .add(
                                    shouldSDKDecodeResponse
                                            ? SdkHttpUtils.urlDecode(commonPrefix)
                                            : commonPrefix);
                }
            } else if (in("ListVersionsResult", "Version")
                    || in("ListVersionsResult", "DeleteMarker")) {

                if ("Key".equals(name)) {
                    currentVersionSummary.setKey(
                            decodeIfSpecified(getText(), shouldSDKDecodeResponse));

                } else if ("VersionId".equals(name)) {
                    currentVersionSummary.setVersionId(getText());

                } else if ("IsLatest".equals(name)) {
                    currentVersionSummary.setIsLatest("true".equals(getText()));

                } else if ("LastModified".equals(name)) {
                    currentVersionSummary.setLastModified(ServiceUtils.parseIso8601Date(getText()));

                } else if ("ETag".equals(name)) {
                    currentVersionSummary.setETag(ServiceUtils.removeQuotes(getText()));

                } else if ("Size".equals(name)) {
                    currentVersionSummary.setSize(Long.parseLong(getText()));

                } else if ("Owner".equals(name)) {
                    currentVersionSummary.setOwner(currentOwner);
                    currentOwner = null;

                } else if ("StorageClass".equals(name)) {
                    currentVersionSummary.setStorageClass(getText());
                }
            } else if (in("ListVersionsResult", "Version", "Owner")
                    || in("ListVersionsResult", "DeleteMarker", "Owner")) {

                if ("ID".equals(name)) {
                    currentOwner.setId(getText());
                } else if ("DisplayName".equals(name)) {
                    currentOwner.setDisplayName(getText());
                }
            }
        }
    }

    public static class BucketWebsiteConfigurationHandler extends AbstractHandler {

        private final BucketWebsiteConfiguration configuration =
                new BucketWebsiteConfiguration(null);

        private RoutingRuleCondition currentCondition = null;
        private RedirectRule currentRedirectRule = null;
        private RoutingRule currentRoutingRule = null;

        public BucketWebsiteConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("WebsiteConfiguration")) {
                if ("RedirectAllRequestsTo".equals(name)) {
                    currentRedirectRule = new RedirectRule();
                }
            } else if (in("WebsiteConfiguration", "RoutingRules")) {
                if ("RoutingRule".equals(name)) {
                    currentRoutingRule = new RoutingRule();
                }
            } else if (in("WebsiteConfiguration", "RoutingRules", "RoutingRule")) {
                if ("Condition".equals(name)) {
                    currentCondition = new RoutingRuleCondition();
                } else if ("Redirect".equals(name)) {
                    currentRedirectRule = new RedirectRule();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("WebsiteConfiguration")) {
                if ("RedirectAllRequestsTo".equals(name)) {
                    configuration.setRedirectAllRequestsTo(currentRedirectRule);
                    currentRedirectRule = null;
                }
            } else if (in("WebsiteConfiguration", "IndexDocument")) {
                if ("Suffix".equals(name)) {
                    configuration.setIndexDocumentSuffix(getText());
                }
            } else if (in("WebsiteConfiguration", "ErrorDocument")) {
                if ("Key".equals(name)) {
                    configuration.setErrorDocument(getText());
                }
            } else if (in("WebsiteConfiguration", "RoutingRules")) {
                if ("RoutingRule".equals(name)) {
                    configuration.getRoutingRules().add(currentRoutingRule);
                    currentRoutingRule = null;
                }
            } else if (in("WebsiteConfiguration", "RoutingRules", "RoutingRule")) {
                if ("Condition".equals(name)) {
                    currentRoutingRule.setCondition(currentCondition);
                    currentCondition = null;
                } else if ("Redirect".equals(name)) {
                    currentRoutingRule.setRedirect(currentRedirectRule);
                    currentRedirectRule = null;
                }
            } else if (in("WebsiteConfiguration", "RoutingRules", "RoutingRule", "Condition")) {
                if ("KeyPrefixEquals".equals(name)) {
                    currentCondition.setKeyPrefixEquals(getText());
                } else if ("HttpErrorCodeReturnedEquals".equals(name)) {
                    currentCondition.setHttpErrorCodeReturnedEquals(getText());
                }
            } else if (in("WebsiteConfiguration", "RedirectAllRequestsTo")
                    || in("WebsiteConfiguration", "RoutingRules", "RoutingRule", "Redirect")) {

                if ("Protocol".equals(name)) {
                    currentRedirectRule.setProtocol(getText());

                } else if ("HostName".equals(name)) {
                    currentRedirectRule.setHostName(getText());

                } else if ("ReplaceKeyPrefixWith".equals(name)) {
                    currentRedirectRule.setReplaceKeyPrefixWith(getText());

                } else if ("ReplaceKeyWith".equals(name)) {
                    currentRedirectRule.setReplaceKeyWith(getText());

                } else if ("HttpRedirectCode".equals(name)) {
                    currentRedirectRule.setHttpRedirectCode(getText());
                }
            }
        }
    }

    public static class BucketVersioningConfigurationHandler extends AbstractHandler {

        private final BucketVersioningConfiguration configuration =
                new BucketVersioningConfiguration();

        public BucketVersioningConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("VersioningConfiguration")) {
                if ("Status".equals(name)) {
                    configuration.setStatus(getText());

                } else if ("MfaDelete".equals(name)) {
                    String mfaDeleteStatus = getText();

                    if ("Disabled".equals(mfaDeleteStatus)) {
                        configuration.setMfaDeleteEnabled(false);
                    } else if ("Enabled".equals(mfaDeleteStatus)) {
                        configuration.setMfaDeleteEnabled(true);
                    } else {
                        configuration.setMfaDeleteEnabled(null);
                    }
                }
            }
        }
    }

    public static class BucketAccelerateConfigurationHandler extends AbstractHandler {

        private final BucketAccelerateConfiguration configuration =
                new BucketAccelerateConfiguration((String) null);

        public BucketAccelerateConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("AccelerateConfiguration")) {
                if ("Status".equals(name)) {
                    configuration.setStatus(getText());
                }
            }
        }
    }

    /*
     * <?xml version="1.0" encoding="UTF-8"?>
     * <CompleteMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
     *     <Location>http://Example-Bucket.s3.amazonaws.com/Example-Object</Location>
     *     <Bucket>Example-Bucket</Bucket>
     *     <Key>Example-Object</Key>
     *     <ETag>"3858f62230ac3c915f300c664312c11f-9"</ETag>
     * </CompleteMultipartUploadResult>
     *
     * Or if an error occurred while completing:
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <Error>
     *     <Code>InternalError</Code>
     *     <Message>We encountered an internal error. Please try again.</Message>
     *     <RequestId>656c76696e6727732072657175657374</RequestId>
     *     <HostId>Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==</HostId>
     * </Error>
     */
    public static class CompleteMultipartUploadHandler extends AbstractSSEHandler
            implements ObjectExpirationResult, S3VersionResult, S3RequesterChargedResult {
        // Successful completion
        private CompleteMultipartUploadResult result;

        // Error during completion
        private AmazonS3Exception ase;
        private String hostId;
        private String requestId;
        private String errorCode;

        @Override
        protected ServerSideEncryptionResult sseResult() {
            return result;
        }
        /** @see CompleteMultipartUploadResult#getExpirationTime() */
        @Override
        public Date getExpirationTime() {
            return result == null ? null : result.getExpirationTime();
        }

        /** @see CompleteMultipartUploadResult#setExpirationTime(Date) */
        @Override
        public void setExpirationTime(Date expirationTime) {
            if (result != null) {
                result.setExpirationTime(expirationTime);
            }
        }

        /** @see CompleteMultipartUploadResult#getExpirationTimeRuleId() */
        @Override
        public String getExpirationTimeRuleId() {
            return result == null ? null : result.getExpirationTimeRuleId();
        }

        /** @see CompleteMultipartUploadResult#setExpirationTimeRuleId(String) */
        @Override
        public void setExpirationTimeRuleId(String expirationTimeRuleId) {
            if (result != null) {
                result.setExpirationTimeRuleId(expirationTimeRuleId);
            }
        }

        @Override
        public void setVersionId(String versionId) {
            if (result != null) {
                result.setVersionId(versionId);
            }
        }

        @Override
        public String getVersionId() {
            return result == null ? null : result.getVersionId();
        }

        /** @see CompleteMultipartUploadResult#isRequesterCharged() */
        public boolean isRequesterCharged() {
            return result == null ? false : result.isRequesterCharged();
        }

        /** @see CompleteMultipartUploadResult#setRequesterCharged(boolean) */
        public void setRequesterCharged(boolean isRequesterCharged) {
            if (result != null) {
                result.setRequesterCharged(isRequesterCharged);
            }
        }

        public CompleteMultipartUploadResult getCompleteMultipartUploadResult() {
            return result;
        }

        public AmazonS3Exception getAmazonS3Exception() {
            return ase;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (atTopLevel()) {
                if ("CompleteMultipartUploadResult".equals(name)) {
                    result = new CompleteMultipartUploadResult();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (atTopLevel()) {
                if ("Error".equals(name)) {
                    if (ase != null) {
                        ase.setErrorCode(errorCode);
                        ase.setRequestId(requestId);
                        ase.setExtendedRequestId(hostId);
                    }
                }
            } else if (in("CompleteMultipartUploadResult")) {
                if ("Location".equals(name)) {
                    result.setLocation(getText());
                } else if ("Bucket".equals(name)) {
                    result.setBucketName(getText());
                } else if ("Key".equals(name)) {
                    result.setKey(getText());
                } else if ("ETag".equals(name)) {
                    result.setETag(ServiceUtils.removeQuotes(getText()));
                }
            } else if (in("Error")) {
                if ("Code".equals(name)) {
                    errorCode = getText();
                } else if ("Message".equals(name)) {
                    ase = new AmazonS3Exception(getText());
                } else if ("RequestId".equals(name)) {
                    requestId = getText();
                } else if ("HostId".equals(name)) {
                    hostId = getText();
                }
            }
        }
    }

    /*
     * <?xml version="1.0" encoding="UTF-8"?>
     * <InitiateMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
     *     <Bucket>example-bucket</Bucket>
     *     <Key>example-object</Key>
     *     <UploadId>VXBsb2FkIElEIGZvciA2aWWpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
     * </InitiateMultipartUploadResult>
     */
    public static class InitiateMultipartUploadHandler extends AbstractHandler {

        private final InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();

        public InitiateMultipartUploadResult getInitiateMultipartUploadResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {}

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("InitiateMultipartUploadResult")) {
                if ("Bucket".equals(name)) {
                    result.setBucketName(getText());

                } else if ("Key".equals(name)) {
                    result.setKey(getText());

                } else if ("UploadId".equals(name)) {
                    result.setUploadId(getText());
                }
            }
        }
    }

    /*
     * HTTP/1.1 200 OK
     * x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
     * x-amz-request-id: 656c76696e6727732072657175657374
     * Date: Tue, 16 Feb 2010 20:34:56 GMT
     * Content-Length: 1330
     * Connection: keep-alive
     * Server: AmazonS3
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <ListMultipartUploadsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
     *     <Bucket>bucket</Bucket>
     *     <KeyMarker></KeyMarker>
     *     <Delimiter>/</Delimiter>
     *     <Prefix/>
     *     <UploadIdMarker></UploadIdMarker>
     *     <NextKeyMarker>my-movie.m2ts</NextKeyMarker>
     *     <NextUploadIdMarker>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</NextUploadIdMarker>
     *     <MaxUploads>3</MaxUploads>
     *     <IsTruncated>true</IsTruncated>
     *     <Upload>
     *         <Key>my-divisor</Key>
     *         <UploadId>XMgbGlrZSBlbHZpbmcncyBub3QgaGF2aW5nIG11Y2ggbHVjaw</UploadId>
     *         <Owner>
     *             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
     *             <DisplayName>delving</DisplayName>
     *         </Owner>
     *         <StorageClass>STANDARD</StorageClass>
     *         <Initiated>Tue, 26 Jan 2010 19:42:19 GMT</Initiated>
     *     </Upload>
     *     <Upload>
     *         <Key>my-movie.m2ts</Key>
     *         <UploadId>VXBsb2FkIElEIGZvciBlbHZpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
     *         <Owner>
     *             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
     *             <DisplayName>delving</DisplayName>
     *         </Owner>
     *         <StorageClass>STANDARD</StorageClass>
     *         <Initiated>Tue, 16 Feb 2010 20:34:56 GMT</Initiated>
     *     </Upload>
     *     <Upload>
     *         <Key>my-movie.m2ts</Key>
     *         <UploadId>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</UploadId>
     *         <Owner>
     *             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
     *             <DisplayName>delving</DisplayName>
     *         </Owner>
     *         <StorageClass>STANDARD</StorageClass>
     *         <Initiated>Wed, 27 Jan 2010 03:02:01 GMT</Initiated>
     *     </Upload>
     *    <CommonPrefixes>
     *        <Prefix>photos/</Prefix>
     *    </CommonPrefixes>
     *    <CommonPrefixes>
     *        <Prefix>videos/</Prefix>
     *    </CommonPrefixes>
     * </ListMultipartUploadsResult>
     */
    public static class ListMultipartUploadsHandler extends AbstractHandler {

        private final MultipartUploadListing result = new MultipartUploadListing();

        private MultipartUpload currentMultipartUpload;
        private Owner currentOwner;

        public MultipartUploadListing getListMultipartUploadsResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListMultipartUploadsResult")) {
                if ("Upload".equals(name)) {
                    currentMultipartUpload = new MultipartUpload();
                }
            } else if (in("ListMultipartUploadsResult", "Upload")) {
                if ("Owner".equals(name) || "Initiator".equals(name)) {
                    currentOwner = new Owner();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("ListMultipartUploadsResult")) {
                if ("Bucket".equals(name)) {
                    result.setBucketName(getText());
                } else if ("KeyMarker".equals(name)) {
                    result.setKeyMarker(checkForEmptyString(getText()));
                } else if ("Delimiter".equals(name)) {
                    result.setDelimiter(checkForEmptyString(getText()));
                } else if ("Prefix".equals(name)) {
                    result.setPrefix(checkForEmptyString(getText()));
                } else if ("UploadIdMarker".equals(name)) {
                    result.setUploadIdMarker(checkForEmptyString(getText()));
                } else if ("NextKeyMarker".equals(name)) {
                    result.setNextKeyMarker(checkForEmptyString(getText()));
                } else if ("NextUploadIdMarker".equals(name)) {
                    result.setNextUploadIdMarker(checkForEmptyString(getText()));
                } else if ("MaxUploads".equals(name)) {
                    result.setMaxUploads(Integer.parseInt(getText()));
                } else if ("EncodingType".equals(name)) {
                    result.setEncodingType(checkForEmptyString(getText()));
                } else if ("IsTruncated".equals(name)) {
                    result.setTruncated(Boolean.parseBoolean(getText()));
                } else if ("Upload".equals(name)) {
                    result.getMultipartUploads().add(currentMultipartUpload);
                    currentMultipartUpload = null;
                }
            } else if (in("ListMultipartUploadsResult", "CommonPrefixes")) {
                if ("Prefix".equals(name)) {
                    result.getCommonPrefixes().add(getText());
                }
            } else if (in("ListMultipartUploadsResult", "Upload")) {
                if ("Key".equals(name)) {
                    currentMultipartUpload.setKey(getText());
                } else if ("UploadId".equals(name)) {
                    currentMultipartUpload.setUploadId(getText());
                } else if ("Owner".equals(name)) {
                    currentMultipartUpload.setOwner(currentOwner);
                    currentOwner = null;
                } else if ("Initiator".equals(name)) {
                    currentMultipartUpload.setInitiator(currentOwner);
                    currentOwner = null;
                } else if ("StorageClass".equals(name)) {
                    currentMultipartUpload.setStorageClass(getText());
                } else if ("Initiated".equals(name)) {
                    currentMultipartUpload.setInitiated(ServiceUtils.parseIso8601Date(getText()));
                }
            } else if (in("ListMultipartUploadsResult", "Upload", "Owner")
                    || in("ListMultipartUploadsResult", "Upload", "Initiator")) {

                if ("ID".equals(name)) {
                    currentOwner.setId(checkForEmptyString(getText()));
                } else if ("DisplayName".equals(name)) {
                    currentOwner.setDisplayName(checkForEmptyString(getText()));
                }
            }
        }
    }

    /*
     * HTTP/1.1 200 OK
     * x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
     * x-amz-request-id: 656c76696e6727732072657175657374
     * Date: Tue, 16 Feb 2010 20:34:56 GMT
     * Content-Length: 985
     * Connection: keep-alive
     * Server: AmazonS3
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <ListPartsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
     *     <Bucket>example-bucket</Bucket>
     *     <Key>example-object</Key>
     *     <UploadId>XXBsb2FkIElEIGZvciBlbHZpbmcncyVcdS1tb3ZpZS5tMnRzEEEwbG9hZA</UploadId>
     *     <Owner>
     *         <ID>x1x16700c70b0b05597d7ecd6a3f92be</ID>
     *         <DisplayName>username</DisplayName>
     *     </Owner>
     *     <Initiator>
     *         <ID>x1x16700c70b0b05597d7ecd6a3f92be</ID>
     *         <DisplayName>username</DisplayName>
     *     </Initiator>
     *     <StorageClass>STANDARD</StorageClass>
     *     <PartNumberMarker>1</PartNumberMarker>
     *     <NextPartNumberMarker>3</NextPartNumberMarker>
     *     <MaxParts>2</MaxParts>
     *     <IsTruncated>true</IsTruncated>
     *     <Part>
     *         <PartNumber>2</PartNumber>
     *         <LastModified>Wed, 27 Jan 2010 03:02:03 GMT</LastModified>
     *         <ETag>"7778aef83f66abc1fa1e8477f296d394"</ETag>
     *         <Size>10485760</Size>
     *     </Part>
     *     <Part>
     *        <PartNumber>3</PartNumber>
     *        <LastModified>Wed, 27 Jan 2010 03:02:02 GMT</LastModified>
     *        <ETag>"aaaa18db4cc2f85cedef654fccc4a4x8"</ETag>
     *        <Size>10485760</Size>
     *     </Part>
     * </ListPartsResult>
     */
    public static class ListPartsHandler extends AbstractHandler {

        private final PartListing result = new PartListing();

        private PartSummary currentPart;
        private Owner currentOwner;

        public PartListing getListPartsResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListPartsResult")) {
                if ("Part".equals(name)) {
                    currentPart = new PartSummary();
                } else if ("Owner".equals(name) || "Initiator".equals(name)) {
                    currentOwner = new Owner();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("ListPartsResult")) {
                if ("Bucket".equals(name)) {
                    result.setBucketName(getText());
                } else if ("Key".equals(name)) {
                    result.setKey(getText());
                } else if ("UploadId".equals(name)) {
                    result.setUploadId(getText());
                } else if ("Owner".equals(name)) {
                    result.setOwner(currentOwner);
                    currentOwner = null;
                } else if ("Initiator".equals(name)) {
                    result.setInitiator(currentOwner);
                    currentOwner = null;
                } else if ("StorageClass".equals(name)) {
                    result.setStorageClass(getText());
                } else if ("PartNumberMarker".equals(name)) {
                    result.setPartNumberMarker(parseInteger(getText()));
                } else if ("NextPartNumberMarker".equals(name)) {
                    result.setNextPartNumberMarker(parseInteger(getText()));
                } else if ("MaxParts".equals(name)) {
                    result.setMaxParts(parseInteger(getText()));
                } else if ("EncodingType".equals(name)) {
                    result.setEncodingType(checkForEmptyString(getText()));
                } else if ("IsTruncated".equals(name)) {
                    result.setTruncated(Boolean.parseBoolean(getText()));
                } else if ("Part".equals(name)) {
                    result.getParts().add(currentPart);
                    currentPart = null;
                }
            } else if (in("ListPartsResult", "Part")) {
                if ("PartNumber".equals(name)) {
                    currentPart.setPartNumber(Integer.parseInt(getText()));
                } else if ("LastModified".equals(name)) {
                    currentPart.setLastModified(ServiceUtils.parseIso8601Date(getText()));
                } else if ("ETag".equals(name)) {
                    currentPart.setETag(ServiceUtils.removeQuotes(getText()));
                } else if ("Size".equals(name)) {
                    currentPart.setSize(Long.parseLong(getText()));
                }
            } else if (in("ListPartsResult", "Owner") || in("ListPartsResult", "Initiator")) {

                if ("ID".equals(name)) {
                    currentOwner.setId(checkForEmptyString(getText()));
                } else if ("DisplayName".equals(name)) {
                    currentOwner.setDisplayName(checkForEmptyString(getText()));
                }
            }
        }

        private Integer parseInteger(String text) {
            text = checkForEmptyString(getText());
            if (text == null) return null;
            return Integer.parseInt(text);
        }
    }

    /**
     * Handler for parsing the get replication configuration response from Amazon S3. Sample HTTP
     * response is given below.
     *
     * <pre>
     * <ReplicationConfiguration>
     * 	<Rule>
     *   	<ID>replication-rule-1-1421862858808</ID>
     *   	<Prefix>testPrefix1</Prefix>
     *   	<Status>Enabled</Status>
     *   	<Destination>
     *       	<Bucket>bucketARN</Bucket>
     *   	</Destination>
     * </Rule>
     * <Rule>
     *   	<ID>replication-rule-2-1421862858808</ID>
     *   	<Prefix>testPrefix2</Prefix>
     *   	<Status>Disabled</Status>
     *   	<Destination>
     *       	<Bucket>arn:aws:s3:::bucket-dest-replication-integ-test-1421862858808</Bucket>
     *   	</Destination>
     * </Rule>
     * </ReplicationConfiguration>
     * </pre>
     */
    public static class BucketReplicationConfigurationHandler extends AbstractHandler {

        private final BucketReplicationConfiguration bucketReplicationConfiguration =
                new BucketReplicationConfiguration();
        private String currentRuleId;
        private ReplicationRule currentRule;
        private ReplicationDestinationConfig destinationConfig;
        private AccessControlTranslation accessControlTranslation;
        private EncryptionConfiguration encryptionConfiguration;
        private SourceSelectionCriteria sourceSelectionCriteria;
        private SseKmsEncryptedObjects sseKmsEncryptedObjects;
        private static final String REPLICATION_CONFIG = "ReplicationConfiguration";
        private static final String ROLE = "Role";
        private static final String RULE = "Rule";
        private static final String DESTINATION = "Destination";
        private static final String ID = "ID";
        private static final String PREFIX = "Prefix";
        private static final String STATUS = "Status";
        private static final String BUCKET = "Bucket";
        private static final String STORAGECLASS = "StorageClass";
        private static final String ACCOUNT = "Account";
        private static final String ACCESS_CONTROL_TRANSLATION = "AccessControlTranslation";
        private static final String OWNER = "Owner";
        private static final String ENCRYPTION_CONFIGURATION = "EncryptionConfiguration";
        private static final String REPLICA_KMS_KEY_ID = "ReplicaKmsKeyID";
        private static final String SOURCE_SELECTION_CRITERIA = "SourceSelectionCriteria";
        private static final String SSE_KMS_ENCRYPTED_OBJECTS = "SseKmsEncryptedObjects";

        public BucketReplicationConfiguration getConfiguration() {
            return bucketReplicationConfiguration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in(REPLICATION_CONFIG)) {
                if (RULE.equals(name)) {
                    currentRule = new ReplicationRule();
                }
            } else if (in(REPLICATION_CONFIG, RULE)) {
                if (DESTINATION.equals(name)) {
                    destinationConfig = new ReplicationDestinationConfig();
                } else if (SOURCE_SELECTION_CRITERIA.equals(name)) {
                    sourceSelectionCriteria = new SourceSelectionCriteria();
                }
            } else if (in(REPLICATION_CONFIG, RULE, DESTINATION)) {
                if (ACCESS_CONTROL_TRANSLATION.equals(name)) {
                    accessControlTranslation = new AccessControlTranslation();
                } else if (ENCRYPTION_CONFIGURATION.equals(name)) {
                    encryptionConfiguration = new EncryptionConfiguration();
                }
            } else if (in(REPLICATION_CONFIG, RULE, SOURCE_SELECTION_CRITERIA)) {
                if (SSE_KMS_ENCRYPTED_OBJECTS.equals(name)) {
                    sseKmsEncryptedObjects = new SseKmsEncryptedObjects();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in(REPLICATION_CONFIG)) {
                if (RULE.equals(name)) {
                    bucketReplicationConfiguration.addRule(currentRuleId, currentRule);
                    currentRule = null;
                    currentRuleId = null;
                    destinationConfig = null;
                    sseKmsEncryptedObjects = null;
                    accessControlTranslation = null;
                    encryptionConfiguration = null;
                } else if (ROLE.equals(name)) {
                    bucketReplicationConfiguration.setRoleARN(getText());
                }
            } else if (in(REPLICATION_CONFIG, RULE)) {
                if (ID.equals(name)) {
                    currentRuleId = getText();
                } else if (PREFIX.equals(name)) {
                    currentRule.setPrefix(getText());
                } else if (SOURCE_SELECTION_CRITERIA.equals(name)) {
                    currentRule.setSourceSelectionCriteria(sourceSelectionCriteria);
                } else {
                    if (STATUS.equals(name)) {
                        currentRule.setStatus(getText());

                    } else if (DESTINATION.equals(name)) {
                        currentRule.setDestinationConfig(destinationConfig);
                    }
                }
            } else if (in(REPLICATION_CONFIG, RULE, SOURCE_SELECTION_CRITERIA)) {
                if (SSE_KMS_ENCRYPTED_OBJECTS.equals(name)) {
                    sourceSelectionCriteria.setSseKmsEncryptedObjects(sseKmsEncryptedObjects);
                }
            } else if (in(
                    REPLICATION_CONFIG,
                    RULE,
                    SOURCE_SELECTION_CRITERIA,
                    SSE_KMS_ENCRYPTED_OBJECTS)) {
                if (STATUS.equals(name)) {
                    sseKmsEncryptedObjects.setStatus(getText());
                }
            } else if (in(REPLICATION_CONFIG, RULE, DESTINATION)) {
                if (BUCKET.equals(name)) {
                    destinationConfig.setBucketARN(getText());
                } else if (STORAGECLASS.equals(name)) {
                    destinationConfig.setStorageClass(getText());
                } else if (ACCOUNT.equals(name)) {
                    destinationConfig.setAccount(getText());
                } else if (ACCESS_CONTROL_TRANSLATION.equals(name)) {
                    destinationConfig.setAccessControlTranslation(accessControlTranslation);
                } else if (ENCRYPTION_CONFIGURATION.equals(name)) {
                    destinationConfig.setEncryptionConfiguration(encryptionConfiguration);
                }
            } else if (in(REPLICATION_CONFIG, RULE, DESTINATION, ACCESS_CONTROL_TRANSLATION)) {
                if (OWNER.equals(name)) {
                    accessControlTranslation.setOwner(getText());
                }
            } else if (in(REPLICATION_CONFIG, RULE, DESTINATION, ENCRYPTION_CONFIGURATION)) {
                if (REPLICA_KMS_KEY_ID.equals(name)) {
                    encryptionConfiguration.setReplicaKmsKeyID(getText());
                }
            }
        }
    }

    public static class BucketTaggingConfigurationHandler extends AbstractHandler {

        private final BucketTaggingConfiguration configuration = new BucketTaggingConfiguration();

        private Map<String, String> currentTagSet;
        private String currentTagKey;
        private String currentTagValue;

        public BucketTaggingConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("Tagging")) {
                if ("TagSet".equals(name)) {
                    currentTagSet = new HashMap<String, String>();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("Tagging")) {
                if ("TagSet".equals(name)) {
                    configuration.getAllTagSets().add(new TagSet(currentTagSet));
                    currentTagSet = null;
                }
            } else if (in("Tagging", "TagSet")) {
                if ("Tag".equals(name)) {
                    if (currentTagKey != null && currentTagValue != null) {
                        currentTagSet.put(currentTagKey, currentTagValue);
                    }
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in("Tagging", "TagSet", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            }
        }
    }

    /**
     * Handler for unmarshalling the response from GET Object Tagging.
     *
     * <p><Tagging> <TagSet> <Tag> <Key>Foo</Key> <Value>1</Value> </Tag> <Tag> <Key>Bar</Key>
     * <Value>2</Value> </Tag> <Tag> <Key>Baz</Key> <Value>3</Value> </Tag> </TagSet> </Tagging>
     */
    public static class GetObjectTaggingHandler extends AbstractHandler {
        private GetObjectTaggingResult getObjectTaggingResult;
        private List<Tag> tagSet;
        private String currentTagValue;
        private String currentTagKey;

        public GetObjectTaggingResult getResult() {
            return getObjectTaggingResult;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {
            if (in("Tagging")) {
                if ("TagSet".equals(name)) {
                    tagSet = new ArrayList<Tag>();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("Tagging")) {
                if ("TagSet".equals(name)) {
                    getObjectTaggingResult = new GetObjectTaggingResult(tagSet);
                    tagSet = null;
                }
            }
            if (in("Tagging", "TagSet")) {
                if ("Tag".equals(name)) {
                    tagSet.add(new Tag(currentTagKey, currentTagValue));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in("Tagging", "TagSet", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            }
        }
    }

    /*
       HTTP/1.1 200 OK
       x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
       x-amz-request-id: 656c76696e6727732072657175657374
       Date: Tue, 20 Sep 2012 20:34:56 GMT
       Content-Type: application/xml
       Transfer-Encoding: chunked
       Connection: keep-alive
       Server: AmazonS3

       <?xml version="1.0" encoding="UTF-8"?>
       <DeleteResult>
           <Deleted>
              <Key>Key</Key>
              <VersionId>Version</VersionId>
           </Deleted>
           <Error>
              <Key>Key</Key>
              <VersionId>Version</VersionId>
              <Code>Code</Code>
              <Message>Message</Message>
           </Error>
           <Deleted>
              <Key>Key</Key>
              <DeleteMarker>true</DeleteMarker>
              <DeleteMarkerVersionId>Version</DeleteMarkerVersionId>
           </Deleted>
       </DeleteResult>
    */
    public static class DeleteObjectsHandler extends AbstractHandler {

        private final DeleteObjectsResponse response = new DeleteObjectsResponse();

        private DeletedObject currentDeletedObject = null;
        private DeleteError currentError = null;

        public DeleteObjectsResponse getDeleteObjectResult() {
            return response;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("DeleteResult")) {
                if ("Deleted".equals(name)) {
                    currentDeletedObject = new DeletedObject();
                } else if ("Error".equals(name)) {
                    currentError = new DeleteError();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("DeleteResult")) {
                if ("Deleted".equals(name)) {
                    response.getDeletedObjects().add(currentDeletedObject);
                    currentDeletedObject = null;
                } else if ("Error".equals(name)) {
                    response.getErrors().add(currentError);
                    currentError = null;
                }
            } else if (in("DeleteResult", "Deleted")) {
                if ("Key".equals(name)) {
                    currentDeletedObject.setKey(getText());

                } else if ("VersionId".equals(name)) {
                    currentDeletedObject.setVersionId(getText());

                } else if ("DeleteMarker".equals(name)) {
                    currentDeletedObject.setDeleteMarker(getText().equals("true"));

                } else if ("DeleteMarkerVersionId".equals(name)) {
                    currentDeletedObject.setDeleteMarkerVersionId(getText());
                }
            } else if (in("DeleteResult", "Error")) {
                if ("Key".equals(name)) {
                    currentError.setKey(getText());

                } else if ("VersionId".equals(name)) {
                    currentError.setVersionId(getText());

                } else if ("Code".equals(name)) {
                    currentError.setCode(getText());

                } else if ("Message".equals(name)) {
                    currentError.setMessage(getText());
                }
            }
        }
    }

    /**
     * HTTP/1.1 200 OK x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
     * x-amz-request-id: 656c76696e6727732072657175657374 Date: Tue, 20 Sep 2012 20:34:56 GMT
     * Content-Length: xxx Connection: keep-alive Server: AmazonS3
     *
     * <p><LifecycleConfiguration> <Rule> <ID>logs-rule</ID> <Prefix>logs/</Prefix>
     * <Status>Enabled</Status> <Filter> <Prefix>logs/</Prefix> <Tag> <Key>key1</Key>
     * <Value>value1</Value> </Tag> <And> <Prefix>logs/</Prefix> <Tag> <Key>key1</Key>
     * <Value>value1</Value> </Tag> <Tag> <Key>key1</Key> <Value>value1</Value> </Tag> </And>
     * </Filter> <Transition> <Days>30</Days> <StorageClass>STANDARD_IA</StorageClass> </Transition>
     * <Transition> <Days>90</Days> <StorageClass>GLACIER</StorageClass> </Transition> <Expiration>
     * <Days>365</Days> </Expiration> <NoncurrentVersionTransition>
     * <NoncurrentDays>7</NoncurrentDays> <StorageClass>STANDARD_IA</StorageClass>
     * </NoncurrentVersionTransition> <NoncurrentVersionTransition>
     * <NoncurrentDays>14</NoncurrentDays> <StorageClass>GLACIER</StorageClass>
     * </NoncurrentVersionTransition> <NoncurrentVersionExpiration>
     * <NoncurrentDays>365</NoncurrentDays> </NoncurrentVersionExpiration> </Rule> <Rule>
     * <ID>image-rule</ID> <Prefix>image/</Prefix> <Status>Enabled</Status> <Transition>
     * <Date>2012-12-31T00:00:00.000Z</Date> <StorageClass>GLACIER</StorageClass> </Transition>
     * <Expiration> <Date>2020-12-31T00:00:00.000Z</Date> </Expiration>
     * <AbortIncompleteMultipartUpload> <DaysAfterInitiation>10</DaysAfterInitiation>
     * </AbortIncompleteMultipartUpload> </Rule> </LifecycleConfiguration>
     */
    public static class BucketLifecycleConfigurationHandler extends AbstractHandler {

        private final BucketLifecycleConfiguration configuration =
                new BucketLifecycleConfiguration(new ArrayList<Rule>());

        private Rule currentRule;
        private Transition currentTransition;
        private NoncurrentVersionTransition currentNcvTransition;
        private AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;
        private LifecycleFilter currentFilter;
        private List<LifecycleFilterPredicate> andOperandsList;
        private String currentTagKey;
        private String currentTagValue;

        public BucketLifecycleConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("LifecycleConfiguration")) {
                if ("Rule".equals(name)) {
                    currentRule = new Rule();
                }
            } else if (in("LifecycleConfiguration", "Rule")) {
                if ("Transition".equals(name)) {
                    currentTransition = new Transition();
                } else if ("NoncurrentVersionTransition".equals(name)) {
                    currentNcvTransition = new NoncurrentVersionTransition();
                } else if ("AbortIncompleteMultipartUpload".equals(name)) {
                    abortIncompleteMultipartUpload = new AbortIncompleteMultipartUpload();
                } else if ("Filter".equals(name)) {
                    currentFilter = new LifecycleFilter();
                }
            } else if (in("LifecycleConfiguration", "Rule", "Filter")) {
                if ("And".equals(name)) {
                    andOperandsList = new ArrayList<LifecycleFilterPredicate>();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("LifecycleConfiguration")) {
                if ("Rule".equals(name)) {
                    configuration.getRules().add(currentRule);
                    currentRule = null;
                }
            } else if (in("LifecycleConfiguration", "Rule")) {
                if ("ID".equals(name)) {
                    currentRule.setId(getText());

                } else if ("Prefix".equals(name)) {
                    currentRule.setPrefix(getText());

                } else if ("Status".equals(name)) {
                    currentRule.setStatus(getText());

                } else if ("Transition".equals(name)) {
                    currentRule.addTransition(currentTransition);
                    currentTransition = null;

                } else if ("NoncurrentVersionTransition".equals(name)) {
                    currentRule.addNoncurrentVersionTransition(currentNcvTransition);
                    currentNcvTransition = null;
                } else if ("AbortIncompleteMultipartUpload".equals(name)) {
                    currentRule.setAbortIncompleteMultipartUpload(abortIncompleteMultipartUpload);
                    abortIncompleteMultipartUpload = null;
                } else if ("Filter".equals(name)) {
                    currentRule.setFilter(currentFilter);
                    currentFilter = null;
                }
            } else if (in("LifecycleConfiguration", "Rule", "Expiration")) {
                if ("Date".equals(name)) {
                    currentRule.setExpirationDate(ServiceUtils.parseIso8601Date(getText()));
                } else if ("Days".equals(name)) {
                    currentRule.setExpirationInDays(Integer.parseInt(getText()));
                } else if ("ExpiredObjectDeleteMarker".equals(name)) {
                    if ("true".equals(getText())) {
                        currentRule.setExpiredObjectDeleteMarker(true);
                    }
                }
            } else if (in("LifecycleConfiguration", "Rule", "Transition")) {
                if ("StorageClass".equals(name)) {
                    currentTransition.setStorageClass(getText());
                } else if ("Date".equals(name)) {
                    currentTransition.setDate(ServiceUtils.parseIso8601Date(getText()));

                } else if ("Days".equals(name)) {
                    currentTransition.setDays(Integer.parseInt(getText()));
                }
            } else if (in("LifecycleConfiguration", "Rule", "NoncurrentVersionExpiration")) {
                if ("NoncurrentDays".equals(name)) {
                    currentRule.setNoncurrentVersionExpirationInDays(Integer.parseInt(getText()));
                }
            } else if (in("LifecycleConfiguration", "Rule", "NoncurrentVersionTransition")) {
                if ("StorageClass".equals(name)) {
                    currentNcvTransition.setStorageClass(getText());
                } else if ("NoncurrentDays".equals(name)) {
                    currentNcvTransition.setDays(Integer.parseInt(getText()));
                }
            } else if (in("LifecycleConfiguration", "Rule", "AbortIncompleteMultipartUpload")) {
                if ("DaysAfterInitiation".equals(name)) {
                    abortIncompleteMultipartUpload.setDaysAfterInitiation(
                            Integer.parseInt(getText()));
                }
            } else if (in("LifecycleConfiguration", "Rule", "Filter")) {
                if ("Prefix".equals(name)) {
                    currentFilter.setPredicate(new LifecyclePrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    currentFilter.setPredicate(
                            new LifecycleTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                } else if ("And".equals(name)) {
                    currentFilter.setPredicate(new LifecycleAndOperator(andOperandsList));
                    andOperandsList = null;
                }
            } else if (in("LifecycleConfiguration", "Rule", "Filter", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in("LifecycleConfiguration", "Rule", "Filter", "And")) {
                if ("Prefix".equals(name)) {
                    andOperandsList.add(new LifecyclePrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    andOperandsList.add(
                            new LifecycleTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in("LifecycleConfiguration", "Rule", "Filter", "And", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            }
        }
    }

    /*
    HTTP/1.1 200 OK
    x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
    x-amz-request-id: 656c76696e6727732072657175657374
    Date: Tue, 20 Sep 2011 20:34:56 GMT
    Content-Length: Some Length
    Connection: keep-alive
    Server: AmazonS3
    <CORSConfiguration>
       <CORSRule>
         <AllowedOrigin>http://www.foobar.com</AllowedOrigin>
         <AllowedMethod>GET</AllowedMethod>
         <MaxAgeSeconds>3000</MaxAgeSec>
         <ExposeHeader>x-amz-server-side-encryption</ExposeHeader>
       </CORSRule>
    </CORSConfiguration>
    */
    public static class BucketCrossOriginConfigurationHandler extends AbstractHandler {

        private final BucketCrossOriginConfiguration configuration =
                new BucketCrossOriginConfiguration(new ArrayList<CORSRule>());

        private CORSRule currentRule;
        private List<AllowedMethods> allowedMethods = null;
        private List<String> allowedOrigins = null;
        private List<String> exposedHeaders = null;
        private List<String> allowedHeaders = null;

        public BucketCrossOriginConfiguration getConfiguration() {
            return configuration;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("CORSConfiguration")) {
                if ("CORSRule".equals(name)) {
                    currentRule = new CORSRule();
                }
            } else if (in("CORSConfiguration", "CORSRule")) {
                if ("AllowedOrigin".equals(name)) {
                    if (allowedOrigins == null) {
                        allowedOrigins = new ArrayList<String>();
                    }
                } else if ("AllowedMethod".equals(name)) {
                    if (allowedMethods == null) {
                        allowedMethods = new ArrayList<AllowedMethods>();
                    }
                } else if ("ExposeHeader".equals(name)) {
                    if (exposedHeaders == null) {
                        exposedHeaders = new ArrayList<String>();
                    }
                } else if ("AllowedHeader".equals(name)) {
                    if (allowedHeaders == null) {
                        allowedHeaders = new LinkedList<String>();
                    }
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("CORSConfiguration")) {
                if ("CORSRule".equals(name)) {
                    currentRule.setAllowedHeaders(allowedHeaders);
                    currentRule.setAllowedMethods(allowedMethods);
                    currentRule.setAllowedOrigins(allowedOrigins);
                    currentRule.setExposedHeaders(exposedHeaders);
                    allowedHeaders = null;
                    allowedMethods = null;
                    allowedOrigins = null;
                    exposedHeaders = null;

                    configuration.getRules().add(currentRule);
                    currentRule = null;
                }
            } else if (in("CORSConfiguration", "CORSRule")) {
                if ("ID".equals(name)) {
                    currentRule.setId(getText());

                } else if ("AllowedOrigin".equals(name)) {
                    allowedOrigins.add(getText());

                } else if ("AllowedMethod".equals(name)) {
                    allowedMethods.add(AllowedMethods.fromValue(getText()));

                } else if ("MaxAgeSeconds".equals(name)) {
                    currentRule.setMaxAgeSeconds(Integer.parseInt(getText()));

                } else if ("ExposeHeader".equals(name)) {
                    exposedHeaders.add(getText());

                } else if ("AllowedHeader".equals(name)) {
                    allowedHeaders.add(getText());
                }
            }
        }
    }

    /*
      HTTP/1.1 200 OK
      x-amz-id-2: ITnGT1y4RyTmXa3rPi4hklTXouTf0hccUjo0iCPjz6FnfIutBj3M7fPGlWO2SEWp
      x-amz-request-id: 51991C342C575321
      Date: Wed, 14 May 2014 02:11:22 GMT
      Server: AmazonS3
      Content-Length: ...

     <?xml version="1.0" encoding="UTF-8"?>
     <MetricsConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
       <Id>metrics-id</Id>
       <Filter>
       <!-- A filter should have only one of Prefix, Tag or And predicate. ->
         <Prefix>prefix</Prefix>
         <Tag>
             <Key>Project</Key>
             <Value>Foo</Value>
         </Tag>
         <And>
           <Prefix>documents/</Prefix>
           <Tag>
             <Key>foo</Key>
             <Value>bar</Value>
           </Tag>
         </And>
       </Filter>
     </MetricsConfiguration>
    */
    public static class GetBucketMetricsConfigurationHandler extends AbstractHandler {

        private final MetricsConfiguration configuration = new MetricsConfiguration();

        private MetricsFilter filter;
        private List<MetricsFilterPredicate> andOperandsList;
        private String currentTagKey;
        private String currentTagValue;

        public GetBucketMetricsConfigurationResult getResult() {
            return new GetBucketMetricsConfigurationResult()
                    .withMetricsConfiguration(configuration);
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("MetricsConfiguration")) {
                if ("Filter".equals(name)) {
                    filter = new MetricsFilter();
                }

            } else if (in("MetricsConfiguration", "Filter")) {
                if ("And".equals(name)) {
                    andOperandsList = new ArrayList<MetricsFilterPredicate>();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("MetricsConfiguration")) {
                if ("Id".equals(name)) {
                    configuration.setId(getText());
                } else if ("Filter".equals(name)) {
                    configuration.setFilter(filter);
                    filter = null;
                }
            } else if (in("MetricsConfiguration", "Filter")) {
                if ("Prefix".equals(name)) {
                    filter.setPredicate(new MetricsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    filter.setPredicate(
                            new MetricsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                } else if ("And".equals(name)) {
                    filter.setPredicate(new MetricsAndOperator(andOperandsList));
                    andOperandsList = null;
                }
            } else if (in("MetricsConfiguration", "Filter", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in("MetricsConfiguration", "Filter", "And")) {
                if ("Prefix".equals(name)) {
                    andOperandsList.add(new MetricsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    andOperandsList.add(
                            new MetricsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in("MetricsConfiguration", "Filter", "And", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            }
        }
    }

    /*
        HTTP/1.1 200 OK
        x-amz-id-2: ITnGT1y4RyTmXa3rPi4hklTXouTf0hccUjo0iCPjz6FnfIutBj3M7fPGlWO2SEWp
        x-amz-request-id: 51991C342C575321
        Date: Wed, 14 May 2014 02:11:22 GMT
        Server: AmazonS3
        Content-Length: ...

        <ListMetricsConfigurationsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
          <MetricsConfiguration>
            ...
          </MetricsConfiguration>
          <IsTruncated>true</IsTruncated>
          <ContinuationToken>token1</ContinuationToken>
          <NextContinuationToken>token2</NextContinuationToken>
        </ListMetricsConfigurationsResult>
    */
    public static class ListBucketMetricsConfigurationsHandler extends AbstractHandler {

        private final ListBucketMetricsConfigurationsResult result =
                new ListBucketMetricsConfigurationsResult();

        private MetricsConfiguration currentConfiguration;
        private MetricsFilter currentFilter;
        private List<MetricsFilterPredicate> andOperandsList;
        private String currentTagKey;
        private String currentTagValue;

        public ListBucketMetricsConfigurationsResult getResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListMetricsConfigurationsResult")) {
                if ("MetricsConfiguration".equals(name)) {
                    currentConfiguration = new MetricsConfiguration();
                }

            } else if (in("ListMetricsConfigurationsResult", "MetricsConfiguration")) {
                if ("Filter".equals(name)) {
                    currentFilter = new MetricsFilter();
                }

            } else if (in("ListMetricsConfigurationsResult", "MetricsConfiguration", "Filter")) {
                if ("And".equals(name)) {
                    andOperandsList = new ArrayList<MetricsFilterPredicate>();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {

            if (in("ListMetricsConfigurationsResult")) {
                if ("MetricsConfiguration".equals(name)) {
                    if (result.getMetricsConfigurationList() == null) {
                        result.setMetricsConfigurationList(new ArrayList<MetricsConfiguration>());
                    }
                    result.getMetricsConfigurationList().add(currentConfiguration);
                    currentConfiguration = null;
                } else if ("IsTruncated".equals(name)) {
                    result.setTruncated("true".equals(getText()));
                } else if ("ContinuationToken".equals(name)) {
                    result.setContinuationToken(getText());
                } else if ("NextContinuationToken".equals(name)) {
                    result.setNextContinuationToken(getText());
                }
            } else if (in("ListMetricsConfigurationsResult", "MetricsConfiguration")) {
                if ("Id".equals(name)) {
                    currentConfiguration.setId(getText());
                } else if ("Filter".equals(name)) {
                    currentConfiguration.setFilter(currentFilter);
                    currentFilter = null;
                }
            } else if (in("ListMetricsConfigurationsResult", "MetricsConfiguration", "Filter")) {
                if ("Prefix".equals(name)) {
                    currentFilter.setPredicate(new MetricsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    currentFilter.setPredicate(
                            new MetricsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                } else if ("And".equals(name)) {
                    currentFilter.setPredicate(new MetricsAndOperator(andOperandsList));
                    andOperandsList = null;
                }
            } else if (in(
                    "ListMetricsConfigurationsResult", "MetricsConfiguration", "Filter", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in(
                    "ListMetricsConfigurationsResult", "MetricsConfiguration", "Filter", "And")) {
                if ("Prefix".equals(name)) {
                    andOperandsList.add(new MetricsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    andOperandsList.add(
                            new MetricsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in(
                    "ListMetricsConfigurationsResult",
                    "MetricsConfiguration",
                    "Filter",
                    "And",
                    "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            }
        }
    }

    /*
        HTTP/1.1 200 OK
        x-amz-id-2: ITnGT1y4RyTmXa3rPi4hklTXouTf0hccUjo0iCPjz6FnfIutBj3M7fPGlWO2SEWp
        x-amz-request-id: 51991C342C575321
        Date: Wed, 14 May 2014 02:11:22 GMT
        Server: AmazonS3
        Content-Length: ...

       <?xml version="1.0" encoding="UTF-8"?>
       <AnalyticsConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
          <Id>XXX</Id>
          <Filter>
            <And>
              <Prefix>documents/</Prefix>
              <Tag>
                <Key>foo</Key>
                <Value>bar</Value>
              </Tag>
            </And>
          </Filter>
          <StorageClassAnalysis>
            <DataExport>
              <OutputSchemaVersion>1</OutputSchemaVersion>
              <Destination>
                <S3BucketDestination>
                  <Format>CSV</Format>
                  <BucketAccountId>123456789</BucketAccountId>
                  <Bucket>destination-bucket</Bucket>
                  <Prefix>destination-prefix</Prefix>
                </S3BucketDestination>
              </Destination>
            </DataExport>
          </StorageClassAnalysis>
       </AnalyticsConfiguration>
    */
    public static class GetBucketAnalyticsConfigurationHandler extends AbstractHandler {

        private final AnalyticsConfiguration configuration = new AnalyticsConfiguration();

        private AnalyticsFilter filter;
        private List<AnalyticsFilterPredicate> andOperandsList;
        private StorageClassAnalysis storageClassAnalysis;
        private StorageClassAnalysisDataExport dataExport;
        private AnalyticsExportDestination destination;
        private AnalyticsS3BucketDestination s3BucketDestination;

        private String currentTagKey;
        private String currentTagValue;

        public GetBucketAnalyticsConfigurationResult getResult() {
            return new GetBucketAnalyticsConfigurationResult()
                    .withAnalyticsConfiguration(configuration);
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("AnalyticsConfiguration")) {
                if ("Filter".equals(name)) {
                    filter = new AnalyticsFilter();
                } else if ("StorageClassAnalysis".equals(name)) {
                    storageClassAnalysis = new StorageClassAnalysis();
                }

            } else if (in("AnalyticsConfiguration", "Filter")) {
                if ("And".equals(name)) {
                    andOperandsList = new ArrayList<AnalyticsFilterPredicate>();
                }

            } else if (in("AnalyticsConfiguration", "StorageClassAnalysis")) {
                if ("DataExport".equals(name)) {
                    dataExport = new StorageClassAnalysisDataExport();
                }

            } else if (in("AnalyticsConfiguration", "StorageClassAnalysis", "DataExport")) {
                if ("Destination".equals(name)) {
                    destination = new AnalyticsExportDestination();
                }

            } else if (in(
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    s3BucketDestination = new AnalyticsS3BucketDestination();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {
            if (in("AnalyticsConfiguration")) {
                if ("Id".equals(name)) {
                    configuration.setId(getText());
                } else if ("Filter".equals(name)) {
                    configuration.setFilter(filter);
                } else if ("StorageClassAnalysis".equals(name)) {
                    configuration.setStorageClassAnalysis(storageClassAnalysis);
                }
            } else if (in("AnalyticsConfiguration", "Filter")) {
                if ("Prefix".equals(name)) {
                    filter.setPredicate(new AnalyticsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    filter.setPredicate(
                            new AnalyticsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                } else if ("And".equals(name)) {
                    filter.setPredicate(new AnalyticsAndOperator(andOperandsList));
                    andOperandsList = null;
                }
            } else if (in("AnalyticsConfiguration", "Filter", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in("AnalyticsConfiguration", "Filter", "And")) {
                if ("Prefix".equals(name)) {
                    andOperandsList.add(new AnalyticsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    andOperandsList.add(
                            new AnalyticsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in("AnalyticsConfiguration", "Filter", "And", "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in("AnalyticsConfiguration", "StorageClassAnalysis")) {
                if ("DataExport".equals(name)) {
                    storageClassAnalysis.setDataExport(dataExport);
                }
            } else if (in("AnalyticsConfiguration", "StorageClassAnalysis", "DataExport")) {
                if ("OutputSchemaVersion".equals(name)) {
                    dataExport.setOutputSchemaVersion(getText());
                } else if ("Destination".equals(name)) {
                    dataExport.setDestination(destination);
                }
            } else if (in(
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    destination.setS3BucketDestination(s3BucketDestination);
                }
            } else if (in(
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination",
                    "S3BucketDestination")) {
                if ("Format".equals(name)) {
                    s3BucketDestination.setFormat(getText());
                } else if ("BucketAccountId".equals(name)) {
                    s3BucketDestination.setBucketAccountId(getText());
                } else if ("Bucket".equals(name)) {
                    s3BucketDestination.setBucketArn(getText());
                } else if ("Prefix".equals(name)) {
                    s3BucketDestination.setPrefix(getText());
                }
            }
        }
    }

    /*
       HTTP/1.1 200 OK
       x-amz-id-2: ITnGT1y4RyTmXa3rPi4hklTXouTf0hccUjo0iCPjz6FnfIutBj3M7fPGlWO2SEWp
       x-amz-request-id: 51991C342C575321
       Date: Wed, 14 May 2014 02:11:22 GMT
       Server: AmazonS3
       Content-Length: ...

       <ListBucketAnalyticsConfigurationsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
         <AnalyticsConfiguration>
           ...
         </AnalyticsConfiguration>
         <IsTruncated>true</IsTruncated>
         <ContinuationToken>token1</ContinuationToken>
         <NextContinuationToken>token2</NextContinuationToken>
       </ListBucketAnalyticsConfigurationsResult>
    */
    public static class ListBucketAnalyticsConfigurationHandler extends AbstractHandler {

        private final ListBucketAnalyticsConfigurationsResult result =
                new ListBucketAnalyticsConfigurationsResult();

        private AnalyticsConfiguration currentConfiguration;
        private AnalyticsFilter currentFilter;
        private List<AnalyticsFilterPredicate> andOperandsList;
        private StorageClassAnalysis storageClassAnalysis;
        private StorageClassAnalysisDataExport dataExport;
        private AnalyticsExportDestination destination;
        private AnalyticsS3BucketDestination s3BucketDestination;
        private String currentTagKey;
        private String currentTagValue;

        public ListBucketAnalyticsConfigurationsResult getResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListBucketAnalyticsConfigurationsResult")) {
                if ("AnalyticsConfiguration".equals(name)) {
                    currentConfiguration = new AnalyticsConfiguration();
                }

            } else if (in("ListBucketAnalyticsConfigurationsResult", "AnalyticsConfiguration")) {
                if ("Filter".equals(name)) {
                    currentFilter = new AnalyticsFilter();
                } else if ("StorageClassAnalysis".equals(name)) {
                    storageClassAnalysis = new StorageClassAnalysis();
                }

            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "Filter")) {
                if ("And".equals(name)) {
                    andOperandsList = new ArrayList<AnalyticsFilterPredicate>();
                }

            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis")) {
                if ("DataExport".equals(name)) {
                    dataExport = new StorageClassAnalysisDataExport();
                }

            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport")) {
                if ("Destination".equals(name)) {
                    destination = new AnalyticsExportDestination();
                }

            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    s3BucketDestination = new AnalyticsS3BucketDestination();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {

            if (in("ListBucketAnalyticsConfigurationsResult")) {
                if ("AnalyticsConfiguration".equals(name)) {
                    if (result.getAnalyticsConfigurationList() == null) {
                        result.setAnalyticsConfigurationList(
                                new ArrayList<AnalyticsConfiguration>());
                    }
                    result.getAnalyticsConfigurationList().add(currentConfiguration);
                    currentConfiguration = null;
                } else if ("IsTruncated".equals(name)) {
                    result.setTruncated("true".equals(getText()));
                } else if ("ContinuationToken".equals(name)) {
                    result.setContinuationToken(getText());
                } else if ("NextContinuationToken".equals(name)) {
                    result.setNextContinuationToken(getText());
                }
            } else if (in("ListBucketAnalyticsConfigurationsResult", "AnalyticsConfiguration")) {
                if ("Id".equals(name)) {
                    currentConfiguration.setId(getText());
                } else if ("Filter".equals(name)) {
                    currentConfiguration.setFilter(currentFilter);
                } else if ("StorageClassAnalysis".equals(name)) {
                    currentConfiguration.setStorageClassAnalysis(storageClassAnalysis);
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "Filter")) {
                if ("Prefix".equals(name)) {
                    currentFilter.setPredicate(new AnalyticsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    currentFilter.setPredicate(
                            new AnalyticsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                } else if ("And".equals(name)) {
                    currentFilter.setPredicate(new AnalyticsAndOperator(andOperandsList));
                    andOperandsList = null;
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "Filter",
                    "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "Filter",
                    "And")) {
                if ("Prefix".equals(name)) {
                    andOperandsList.add(new AnalyticsPrefixPredicate(getText()));
                } else if ("Tag".equals(name)) {
                    andOperandsList.add(
                            new AnalyticsTagPredicate(new Tag(currentTagKey, currentTagValue)));
                    currentTagKey = null;
                    currentTagValue = null;
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "Filter",
                    "And",
                    "Tag")) {
                if ("Key".equals(name)) {
                    currentTagKey = getText();
                } else if ("Value".equals(name)) {
                    currentTagValue = getText();
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis")) {
                if ("DataExport".equals(name)) {
                    storageClassAnalysis.setDataExport(dataExport);
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport")) {
                if ("OutputSchemaVersion".equals(name)) {
                    dataExport.setOutputSchemaVersion(getText());
                } else if ("Destination".equals(name)) {
                    dataExport.setDestination(destination);
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    destination.setS3BucketDestination(s3BucketDestination);
                }
            } else if (in(
                    "ListBucketAnalyticsConfigurationsResult",
                    "AnalyticsConfiguration",
                    "StorageClassAnalysis",
                    "DataExport",
                    "Destination",
                    "S3BucketDestination")) {
                if ("Format".equals(name)) {
                    s3BucketDestination.setFormat(getText());
                } else if ("BucketAccountId".equals(name)) {
                    s3BucketDestination.setBucketAccountId(getText());
                } else if ("Bucket".equals(name)) {
                    s3BucketDestination.setBucketArn(getText());
                } else if ("Prefix".equals(name)) {
                    s3BucketDestination.setPrefix(getText());
                }
            }
        }
    }

    /*
        HTTP/1.1 200 OK
        x-amz-id-2: Uuag1LuByRx9e6j5Onimru9pO4ZVKnJ2Qz7/C1NPcfTWAtRPfTaOFg==
        x-amz-request-id: 656c76696e6727732072657175657374
        Date: Tue, 20 Sep 2012 20:34:56 GMT
        Content-Length: xxx
        Connection: keep-alive
        Server: AmazonS3

       <InventoryConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
          <Destination>
             <S3BucketDestination>
                <AccountId>A2OCNCIEQW9MSG</AccountId>
                <Bucket>s3-object-inventory-list-gamma-us-east-1</Bucket>
                <Format>CSV</Format>
                <Prefix>string</Prefix>
             </S3BucketDestination>
          </Destination>
          <IsEnabled>true</IsEnabled>
          <Filter>
             <Prefix>string</Prefix>
          </Filter>
          <Id>configId</Id>
          <IncludedObjectVersions>All</IncludedObjectVersions>
          <OptionalFields>
             <Field>Size</Field>
             <Field>LastModifiedDate</Field>
             <Field>StorageClass</Field>
             <Field>ETag</Field>
             <Field>IsMultipartUploaded</Field>
             <Field>ReplicationStatus</Field>
          </OptionalFields>
          <Schedule>
             <Frequency>Daily</Frequency>
          </Schedule>
       </InventoryConfiguration>
    */
    public static class GetBucketInventoryConfigurationHandler extends AbstractHandler {

        public static final String SSE_S3 = "SSE-S3";
        public static final String SSE_KMS = "SSE-KMS";
        private final GetBucketInventoryConfigurationResult result =
                new GetBucketInventoryConfigurationResult();
        private final InventoryConfiguration configuration = new InventoryConfiguration();

        private List<String> optionalFields;
        private InventoryDestination inventoryDestination;
        private InventoryFilter filter;
        private InventoryS3BucketDestination s3BucketDestination;
        private InventorySchedule inventorySchedule;

        public GetBucketInventoryConfigurationResult getResult() {
            return result.withInventoryConfiguration(configuration);
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("InventoryConfiguration")) {
                if ("Destination".equals(name)) {
                    inventoryDestination = new InventoryDestination();
                } else if ("Filter".equals(name)) {
                    filter = new InventoryFilter();
                } else if ("Schedule".equals(name)) {
                    inventorySchedule = new InventorySchedule();
                } else if ("OptionalFields".equals(name)) {
                    optionalFields = new ArrayList<String>();
                }

            } else if (in("InventoryConfiguration", "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    s3BucketDestination = new InventoryS3BucketDestination();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {

            if (in("InventoryConfiguration")) {
                if ("Id".equals(name)) {
                    configuration.setId(getText());

                } else if ("Destination".equals(name)) {
                    configuration.setDestination(inventoryDestination);
                    inventoryDestination = null;

                } else if ("IsEnabled".equals(name)) {
                    configuration.setEnabled("true".equals(getText()));

                } else if ("Filter".equals(name)) {
                    configuration.setInventoryFilter(filter);
                    filter = null;

                } else if ("IncludedObjectVersions".equals(name)) {
                    configuration.setIncludedObjectVersions(getText());

                } else if ("Schedule".equals(name)) {
                    configuration.setSchedule(inventorySchedule);
                    inventorySchedule = null;

                } else if ("OptionalFields".equals(name)) {
                    configuration.setOptionalFields(optionalFields);
                    optionalFields = null;
                }

            } else if (in("InventoryConfiguration", "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    inventoryDestination.setS3BucketDestination(s3BucketDestination);
                    s3BucketDestination = null;
                }

            } else if (in("InventoryConfiguration", "Destination", "S3BucketDestination")) {
                if ("AccountId".equals(name)) {
                    s3BucketDestination.setAccountId(getText());
                } else if ("Bucket".equals(name)) {
                    s3BucketDestination.setBucketArn(getText());
                } else if ("Format".equals(name)) {
                    s3BucketDestination.setFormat(getText());
                } else if ("Prefix".equals(name)) {
                    s3BucketDestination.setPrefix(getText());
                }
            } else if (in(
                    "InventoryConfiguration", "Destination", "S3BucketDestination", "Encryption")) {
                if (SSE_S3.equals(name)) {
                    s3BucketDestination.setEncryption(new ServerSideEncryptionS3());
                } else if (SSE_KMS.equals(name)) {
                    ServerSideEncryptionKMS kmsEncryption =
                            new ServerSideEncryptionKMS().withKeyId(getText());
                    s3BucketDestination.setEncryption(kmsEncryption);
                }
            } else if (in("InventoryConfiguration", "Filter")) {
                if ("Prefix".equals(name)) {
                    filter.setPredicate(new InventoryPrefixPredicate(getText()));
                }

            } else if (in("InventoryConfiguration", "Schedule")) {
                if ("Frequency".equals(name)) {
                    inventorySchedule.setFrequency(getText());
                }

            } else if (in("InventoryConfiguration", "OptionalFields")) {
                if ("Field".equals(name)) {
                    optionalFields.add(getText());
                }
            }
        }
    }

    /*
           HTTP/1.1 200 OK
           x-amz-id-2: ITnGT1y4RyTmXa3rPi4hklTXouTf0hccUjo0iCPjz6FnfIutBj3M7fPGlWO2SEWp
           x-amz-request-id: 51991C342C575321
           Date: Wed, 14 May 2014 02:11:22 GMT
           Server: AmazonS3
           Content-Length: ...

           <ListInventoryConfigurationsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
             <InventoryConfiguration>
               ...
             </InventoryConfiguration>
             <InventoryConfiguration>
               ...
             </InventoryConfiguration>
             <IsTruncated>true</IsTruncated>
             <ContinuationToken>token1</ContinuationToken>
             <NextContinuationToken>token2</NextContinuationToken>
           </ListInventoryConfigurationsResult>
    */
    public static class ListBucketInventoryConfigurationsHandler extends AbstractHandler {

        private final ListBucketInventoryConfigurationsResult result =
                new ListBucketInventoryConfigurationsResult();

        private InventoryConfiguration currentConfiguration;
        private List<String> currentOptionalFieldsList;
        private InventoryDestination currentDestination;
        private InventoryFilter currentFilter;
        private InventoryS3BucketDestination currentS3BucketDestination;
        private InventorySchedule currentSchedule;

        public ListBucketInventoryConfigurationsResult getResult() {
            return result;
        }

        @Override
        protected void doStartElement(String uri, String name, String qName, Attributes attrs) {

            if (in("ListInventoryConfigurationsResult")) {
                if ("InventoryConfiguration".equals(name)) {
                    currentConfiguration = new InventoryConfiguration();
                }

            } else if (in("ListInventoryConfigurationsResult", "InventoryConfiguration")) {
                if ("Destination".equals(name)) {
                    currentDestination = new InventoryDestination();
                } else if ("Filter".equals(name)) {
                    currentFilter = new InventoryFilter();
                } else if ("Schedule".equals(name)) {
                    currentSchedule = new InventorySchedule();
                } else if ("OptionalFields".equals(name)) {
                    currentOptionalFieldsList = new ArrayList<String>();
                }

            } else if (in(
                    "ListInventoryConfigurationsResult", "InventoryConfiguration", "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    currentS3BucketDestination = new InventoryS3BucketDestination();
                }
            }
        }

        @Override
        protected void doEndElement(String uri, String name, String qName) {

            if (in("ListInventoryConfigurationsResult")) {
                if ("InventoryConfiguration".equals(name)) {
                    if (result.getInventoryConfigurationList() == null) {
                        result.setInventoryConfigurationList(
                                new ArrayList<InventoryConfiguration>());
                    }
                    result.getInventoryConfigurationList().add(currentConfiguration);
                    currentConfiguration = null;
                } else if ("IsTruncated".equals(name)) {
                    result.setTruncated("true".equals(getText()));
                } else if ("ContinuationToken".equals(name)) {
                    result.setContinuationToken(getText());
                } else if ("NextContinuationToken".equals(name)) {
                    result.setNextContinuationToken(getText());
                }
            } else if (in("ListInventoryConfigurationsResult", "InventoryConfiguration")) {
                if ("Id".equals(name)) {
                    currentConfiguration.setId(getText());

                } else if ("Destination".equals(name)) {
                    currentConfiguration.setDestination(currentDestination);
                    currentDestination = null;

                } else if ("IsEnabled".equals(name)) {
                    currentConfiguration.setEnabled("true".equals(getText()));

                } else if ("Filter".equals(name)) {
                    currentConfiguration.setInventoryFilter(currentFilter);
                    currentFilter = null;

                } else if ("IncludedObjectVersions".equals(name)) {
                    currentConfiguration.setIncludedObjectVersions(getText());

                } else if ("Schedule".equals(name)) {
                    currentConfiguration.setSchedule(currentSchedule);
                    currentSchedule = null;

                } else if ("OptionalFields".equals(name)) {
                    currentConfiguration.setOptionalFields(currentOptionalFieldsList);
                    currentOptionalFieldsList = null;
                }

            } else if (in(
                    "ListInventoryConfigurationsResult", "InventoryConfiguration", "Destination")) {
                if ("S3BucketDestination".equals(name)) {
                    currentDestination.setS3BucketDestination(currentS3BucketDestination);
                    currentS3BucketDestination = null;
                }

            } else if (in(
                    "ListInventoryConfigurationsResult",
                    "InventoryConfiguration",
                    "Destination",
                    "S3BucketDestination")) {
                if ("AccountId".equals(name)) {
                    currentS3BucketDestination.setAccountId(getText());
                } else if ("Bucket".equals(name)) {
                    currentS3BucketDestination.setBucketArn(getText());
                } else if ("Format".equals(name)) {
                    currentS3BucketDestination.setFormat(getText());
                } else if ("Prefix".equals(name)) {
                    currentS3BucketDestination.setPrefix(getText());
                }

            } else if (in(
                    "ListInventoryConfigurationsResult", "InventoryConfiguration", "Filter")) {
                if ("Prefix".equals(name)) {
                    currentFilter.setPredicate(new InventoryPrefixPredicate(getText()));
                }

            } else if (in(
                    "ListInventoryConfigurationsResult", "InventoryConfiguration", "Schedule")) {
                if ("Frequency".equals(name)) {
                    currentSchedule.setFrequency(getText());
                }

            } else if (in(
                    "ListInventoryConfigurationsResult",
                    "InventoryConfiguration",
                    "OptionalFields")) {
                if ("Field".equals(name)) {
                    currentOptionalFieldsList.add(getText());
                }
            }
        }
    }

    private static String findAttributeValue(String qnameToFind, Attributes attrs) {

        for (int i = 0; i < attrs.getLength(); i++) {
            String qname = attrs.getQName(i);
            if (qname.trim().equalsIgnoreCase(qnameToFind.trim())) {
                return attrs.getValue(i);
            }
        }

        return null;
    }
}
