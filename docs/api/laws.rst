**NYS Laws API**
================

In order to utilize the Laws API we'll go over some quick terminology:

:Law Id:
    A three letter code that identifies the law, e.g. EDN for Education Law.
:Document Id:
    A string that identifies a particular document within a body of law. Contains the three letter law id
    so it can uniquely identify a document.
:Location Id:
    This is simply the document id without the three letter law id prefix.
:Published Date:
    Since the nature of laws are fairly temporal we keep track of changes to the laws using the published date.
    It's possible to retrieve law documents roughly as they were represented during any week since October 2014.

----------

Get a list of law ids
---------------------

**Usage**
::

   (GET) /api/3/laws

**Optional Params**

+-----------+--------------------+--------------------------------------------------------+
| Parameter | Values             | Description                                            |
+===========+====================+========================================================+
| limit     | 1 - 1000           | Number of results to return                            |
+-----------+--------------------+--------------------------------------------------------+
| offset    | >= 1               | Result number to start from                            |
+-----------+--------------------+--------------------------------------------------------+

**Response**

.. code-block:: javascript

    {
        "success": true,
        "message": "Listing of consolidated and unconsolidated NYS Laws",
        "responseType": "law-info list",
        "total": 134,
        "offsetStart": 1,
        "offsetEnd": 134,
        "limit": 0,
        "result": {
            "items": [
                {
                    "lawId": "ABC",
                    "name": "Alcoholic Beverage Control",
                    "lawType": "CONSOLIDATED",
                    "chapter": "3-B"
                },
                {
                    "lawId": "ABP",
                    "name": "Abandoned Property",
                    "lawType": "CONSOLIDATED",
                    "chapter": "1"
                },
        (truncated)

The *lawId* in the response is the three letter id for the law.

Get the law structure
---------------------

Laws are represented as a collection of sub documents, each of which is structured within a hierarchy.
The following call will provide the structure of the law.

**Usage**
::

    (GET) /api/3/laws/{lawId}

**Optional Params**

+-----------+--------------------+--------------------------------------------------------------+
| Parameter | Values             | Description                                                  |
+===========+====================+==============================================================+
| date      | ISO date           | Fetch law structure as it appeared prior to or on this date. |
+-----------+--------------------+--------------------------------------------------------------+
| full      | boolean            | If set to true, all the law text will also be returned.      |
+-----------+--------------------+--------------------------------------------------------------+

**Examples**
::

    /api/3/laws/ABC                  // Get latest law structure for ABC law
    /api/3/laws/TAX?date=2015-01-01  // Get law structure for TAX law as it appeared on or before 01/01/2015
    /api/3/laws/EDN?full=true        // Get latest law structure for EDN law as well as the text body of the law

**Response**

.. code-block:: javascript

    // /api/3/laws/RPT
    {
        "success": true,
        "message": "The document structure for RPT law",
        "responseType": "law-tree",
        "result": {
        "lawVersion": {
            "lawId": "RPT",                                // Three letter law id
            "activeDate": "2015-01-02"                     // Date on which this law content was up to date
        },
        "info": {
            "lawId": "RPT",
            "name": "Real Property Tax",                   // Name of the law
            "lawType": "CONSOLIDATED",                     // One of CONSOLIDATED, UNCONSOLIDATED, COURT_ACTS, RULES, MISC
            "chapter": "50-A"                              // Chapter of law
        },
        "documents": {                                     // This is a repeating document structure
            "lawId": "RPT",
            "locationId": "-CH50-A",                       // The location id identifes this sub document within this law
            "title": "Real Property Tax",                  // Title of this sub document if available.
            "docType": "CHAPTER",
            "docLevelId": "50-A",                          // The doc level id identifies the sub document within the current
                                                           // level in the hierarchy. For example if 'docType' is ARTICLE
                                                           // and docLevelId is 1, it means this is Article 1.

            "activeDate": "2014-09-22",                    // Date this particular document was updated
            "sequenceNo": 1,                               // Preserves ordering of sub documents
            "repealedDate": null,                          // Date this document was repealed (if applicable)
            "repealed": false,                             // This will be true if the document was repealed,
                                                           // 'repealedDate' will be set with the date.
            "text": null,                                  // Text of this document (only set when ?full=true)
            "documents": {                                 // Contains the sub documents of this document
                "items": [                                 // It's a recursive structure
                {
                    "lawId": "RPT",
                    "locationId": "A1",
                    "title": "SHORT TITLE; DEFINITIONS",
                    "docType": "ARTICLE",
                    "docLevelId": "1",
                    "activeDate": "2014-09-22",
                    "sequenceNo": 2,
                    "repealedDate": null,
                    "text": null,
                    "documents": {
                        "items": [
                            {
                                "lawId": "RPT",
                                "locationId": "100",
                                "title": "Short title",
                                "docType": "SECTION",
                                "docLevelId": "100",
                                "activeDate": "2014-09-22",
                                "sequenceNo": 3,
                                "repealedDate": null,
                                "text": null,
                                "documents": {              // Note there are no sub documents for this doc
                                    "items": [],
                                    "size": 0
                                },
                                "repealed": false
                            },
        (truncated)

When the request parameter **full** is set to true the 'text' fields within all the sub-documents will contain
the text body. Note that response can be rather large (several MB) for certain laws so keep that in mind.

If you want to retrieve a specific law document use the following API call:

Get a law sub document
----------------------

**Usage**
::

    (GET) /api/3/laws/{lawId}/{locationId}

The lawId once again is the three letter code (e.g. EDN, TAX) and locationId is the identifier for the sub document.
You can discover the locationId when you make an API request for the law structure (see above section). See the
'locationId' field for that response.

**Examples**
::

    /api/3/laws/TAX/8/     // Get section 8 of Tax law
    /api/3/laws/EDN/A2/    // Get article 2 of Education law

.. note:: A trailing slash is important for this API call because the locationId may have periods which would otherwise be interpreted as an extension of sorts. When in doubt, try adding the trailing slash.

**Response**

The response here is straight-forward.

.. code-block:: javascript

    {
        "success" : true,
        "message" : "Law document for location A2 in EDN law ",
        "responseType" : "law-doc-info-detail",
        "result" : {
            "lawId" : "EDN",
            "lawName" : "Education",
            "locationId" : "A2",
            "title" : "Dignity For All Students",
            "docType" : "ARTICLE",
            "docLevelId" : "2",
            "activeDate" : "2019-05-03",
            "text" : "ARTICLE 2 (text truncated for brevity)"  // The text body of the law will be here,
            "parentLocationIds" : [ "-CH16", "T1" ],
            "parents" : [ {
              "lawId" : "EDN",
              "lawName" : "Education",
              "locationId" : "-CH16",
              "title" : "Education",
              "docType" : "CHAPTER",
              "docLevelId" : "16",
              "activeDate" : "2019-11-01"
            }, {
              "lawId" : "EDN",
              "lawName" : "Education",
              "locationId" : "T1",
              "title" : "General Provisions Article 1 Short Title and Definitions (§§",
              "docType" : "TITLE",
              "docLevelId" : "1",
              "activeDate" : "2019-04-19"
            } ],
            "prevSibling" : {
              "lawId" : "EDN",
              "lawName" : "Education",
              "locationId" : "A1",
              "title" : "Short Title and Definitions",
              "docType" : "ARTICLE",
              "docLevelId" : "1",
              "activeDate" : "2014-09-22"
            },
            "nextSibling" : {
              "lawId" : "EDN",
              "lawName" : "Education",
              "locationId" : "A3",
              "title" : "Education Department",
              "docType" : "ARTICLE",
              "docLevelId" : "3",
              "activeDate" : "2014-09-22"
            }
          }
    }

If the law document was not found you will receive an error response

.. code-block:: javascript

    {
        "success": false,
        "message": "The requested law document was not found",
        "responseType": "error",
        "errorCode": 21,
        "errorData": {
            "lawDocId": "EDNA22",
            "endDate": "2015-01-09"
        },
        "errorDataType": "law-doc-query"
    }

Search for law documents
------------------------

**Usage**
::

    (GET) /api/3/laws/search?term=           // Search across all law volumes
    (GET) /api/3/laws/{lawId}/search?term=   // Search within a specific law volume

**Required Params**

+-----------+--------------------+--------------------------------------------------------------+
| Parameter | Values             | Description                                                  |
+===========+====================+==============================================================+
| term      | string             | The full text search term.                                   |
+-----------+--------------------+--------------------------------------------------------------+

**Optional Params**

+-----------+--------------------+--------------------------------------------------------------+
| Parameter | Values             | Description                                                  |
+===========+====================+==============================================================+
| sort      | string             | Sort using any field from the result object, e.g. lawId:ASC  |
+-----------+--------------------+--------------------------------------------------------------+
| limit     | 1 - 1000           | Number of results to return (high limits take longer)        |
+-----------+--------------------+--------------------------------------------------------------+
| offset    | >= 1               | Result number to start from                                  |
+-----------+--------------------+--------------------------------------------------------------+

**Examples**
::

    /api/3/laws/search?term=chickens                            // Search all law volumes for the word 'chickens'

Get law document updates
------------------------

To identify which documents have been modified or added to a body of law, use the law updates API.

To detect updates to the structure of the law document tree, see `Get law tree updates`_.

.. note:: Law updates are received in a batch update on a weekly basis, so updates that occur during the week will only be visible at the end of that week.

**Usage**

List of laws updated during the given date/time range::

    /api/3/laws/updates/{fromDateTime}/{toDateTime}

.. note:: The fromDateTime and toDateTime should be formatted as the ISO Date Time format. For example December 10, 2014, 1:30:02 PM should be inputted as 2014-12-10T13:30:02. The fromDateTime and toDateTime range is exclusive.

All updates made on a specific body of law::

    /api/3/laws/{lawId}/updates/

    e.g. /api/3/laws/ABC/updates/
         /api/3/laws/VAT/updates/

All updates made on a specific body of law during a date/time range::

    /api/3/laws/{lawId}/updates/{fromDateTime}/{toDateTime}


**Optional Params**

+-----------+----------------------+--------------------------------------------------------+
| Parameter | Values               | Description                                            |
+===========+======================+========================================================+
| type      | (processed|published)| The type of law update                                 |
+-----------+----------------------+--------------------------------------------------------+
| detail    | boolean              | Set to true for updates to individual law documents.   |
+-----------+----------------------+--------------------------------------------------------+
| order     | string (asc|desc)    | Order the results by update date/time                  |
+-----------+----------------------+--------------------------------------------------------+
| limit     | 1 - 1000             | Number of results to return (high limits take longer)  |
+-----------+----------------------+--------------------------------------------------------+
| offset    | >= 1                 | Result number to start from                            |
+-----------+----------------------+--------------------------------------------------------+

**Response**

Global law updates::

    e.g. /api/3/laws/updates/2015-09-01T00:00:00/2015-10-01T00:00:00?type=published

.. _law-update-token-response:

.. code-block:: javascript

    {
        success: true,
        message: "",
        responseType: "update-token list",
        total: 33,
        offsetStart: 1,
        offsetEnd: 33,
        limit: 50,
        result: {
        items: [
            {
                id: {
                    lawId: "RSS",                // Which body of law was updated
                    activeDate: "2015-08-07"     // The active published date
                },
                contentType: "LAW",
                sourceId: "20150807.UPDATE",
                sourceDateTime: "2015-08-07T00:00",   // Date of the source data
                processedDateTime: "2015-09-10T15:00:14.551822"  // Date we processed this update
            },  (truncated..)

Detailed law doc updates::

    e.g. /api/3/laws/updates/2015-09-01T00:00:00/2015-10-01T00:00:00?detail=true&type=published
         /api/3/laws/ABC/updates/

.. _law-update-digest-response:

.. code-block:: javascript

    {
        success: true,
        message: "",
        responseType: "update-digest list",
        total: 431,
        offsetStart: 1,
        offsetEnd: 50,
        limit: 50,
        result: {
        items: [
            {
                id: {
                    lawId: "ABC",
                    locationId: "120",                        // Location id of doc that was updated
                    publishedDate: "2014-09-22"               // Published date of this doc
                },
                contentType: "LAW",
                sourceId: "DATABASE.LAW3",
                sourceDateTime: "2014-09-22T00:00",
                processedDateTime: "2015-06-04T14:36:01.426676",
                action: "Insert",
                scope: "Law Document",
                fields: { },
                fieldCount: 0
            },

Get law tree updates
--------------------

Gets a list of laws which have had structural changes over a specified date/time range.

To see updates to the content of law documents, see `Get law document updates`_.

**Usage**

List of laws with tree updates during the given date/time range
::

    /api/3/laws/tree/updates
    /api/3/laws/tree/updates/{fromDateTime}?type=published
    /api/3/laws/tree/updates/{fromDateTime}/{toDateTime}?type=published

The fromDateTime and toDateTime should be formatted as the ISO Date Time format.
For example December 10, 2014, 1:30:02 PM should be inputted as 2014-12-10T13:30:02.
The fromDateTime and toDateTime range is exclusive.
If excluded, fromDateTime defaults to Jan 1 1970 and toDateTime defaults to the current datetime


**Optional Params**

+-----------+----------------------+--------------------------------------------------------+
| Parameter | Values               | Description                                            |
+===========+======================+========================================================+
| type      | (processed|published)| The type of law update                                 |
+-----------+----------------------+--------------------------------------------------------+
| order     | string (asc|desc)    | Order the results by update date/time                  |
+-----------+----------------------+--------------------------------------------------------+
| limit     | 1 - 1000             | Number of results to return (high limits take longer)  |
+-----------+----------------------+--------------------------------------------------------+
| offset    | >= 1                 | Result number to start from                            |
+-----------+----------------------+--------------------------------------------------------+

**Response**

Get law tree updates by published date::

    e.g. /api/3/laws/tree/updates/2016-10-06/2016-10-08?type=published

.. code-block:: javascript

    {
        success: true,
        message: "",
        responseType: "update-token list",
        total: 23,
        offsetStart: 1,
        offsetEnd: 23,
        limit: 50,
        result: {
            items: [
                {
                    id: {
                        lawId: "PAR",                   // Location id of doc that was updated
                        activeDate: "2016-10-07"        // Published date of this doc
                    },
                    contentType: "LAW",
                    sourceId: "20161007.UPDATE",
                    sourceDateTime: "2016-10-07T00:00",
                    processedDateTime: "2017-08-15T12:40:02.271155"
                },
                (truncated)

Get repealed laws
-----------------

Gets a list of law documents that have been repealed with the option of restricting to a specified date/time range.
The date/time range applies to the processed date of the law document, not the official repeal date.

**Usage**

List of laws repealed during the given date/time range::

    /api/3/laws/repealed

**Optional Params**

+--------------+----------------------+---------------------------------------------------------------------------+
| Parameter    | Values               | Description                                                               |
+==============+======================+===========================================================================+
| fromDateTime | ISO 8601 datetime    | default 1970-01-01 - The inclusive start time of the specified time range |
+--------------+----------------------+---------------------------------------------------------------------------+
| toDateTime   | ISO 8601 datetime    | default now - The inclusive end time of the specified time period         |
+--------------+----------------------+---------------------------------------------------------------------------+

Get a law pdf
------------------------

**Usage**

Retrieve law pdf by document id.
Inputting only the law ID will get you the root node for that chapter::

    (GET) /pdf/laws/{docId}

**Optional Params**

+-----------+----------------------+--------------------------------------------------------+
| Parameter | Values               | Description                                            |
+===========+======================+========================================================+
| full      | (true|false)         | Whether to get all the children of this document.      |
+-----------+----------------------+--------------------------------------------------------+

**Examples**

Request law document CC0A1 and its children::

    /api/3/laws/CCOA1?full=true
