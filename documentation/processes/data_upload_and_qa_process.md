# Dataland Data Upload And QA Process (Technical)
| Metadata        | Value                                                                                                                                                                                                                   |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Target Audience | Dataland Developers                                                                                                                                                                                                     |
| Abstract        | This document describes the process of uploading data to Dataland and the QA process that follows. It focuses on any messages that are sent via the message-queue. It is limited to **public** datasets (i.e., no VSME) |


The following diagram describes the process of uploading data to Dataland and the QA process that follows. It focuses on any messages that are sent via the message-queue.
Please note that all message-queue events are asynchronous and are not necessarily in the order they are shown in the diagram.

```mermaid
sequenceDiagram
    actor Uploader
    participant mq as Message Queue
    participant backend as Dataland Backend
    participant storage as Internal Storage
    participant autoQa as Automated QA
    participant qaService as QA Service
    participant community as Community Manager
    actor qaler as QA Provider
    Uploader ->> backend: Upload dataset
    activate backend
    backend ->> backend: Store Meta-Data
    backend ->> backend: Store Dataset in Temporary Storage
    backend --) mq: Send 'Public Data received'
    activate mq
    backend -->> Uploader: Upload completed
    deactivate backend
    mq -) storage: Receive 'Public Data received'
    deactivate mq
    activate storage
    storage ->> backend: Get Dataset from Temporary Storage
    activate backend
    backend -->> storage: Dataset
    deactivate backend
    storage ->> storage: Store Dataset in Database
    storage --) mq: Send 'Data Stored'
    activate mq
    deactivate storage

    mq -) backend: Receive 'Data Stored'
    activate backend
    backend ->> backend: Remove Dataset from Temporary Storage
    deactivate backend
    alt IF qa NOT bypassed
        mq -) autoQa: Receive 'Data Stored'
        deactivate mq
        activate autoQa
        autoQa --) mq: Send 'Manual QA requested'
        deactivate autoQa
        activate mq

        mq -) qaService: Receive 'Manual QA Request'
        deactivate mq
        activate qaService
        qaService ->> qaService: Store Dataset in QA Queue
        deactivate qaService

        qaler ->> qaService: Get Datasets to QA
        activate qaService
        activate qaler
        qaService -->> qaler: Dataset List
        deactivate qaService
        qaler ->> qaService: Quality Assured
        deactivate qaler
        activate qaService
        qaService ->> qaService: Store decision in Database
        
        qaService --) mq: Send 'QA Completed'
        deactivate qaService
        
    else QA Bypassed:

        mq -) autoQa: Receive 'Data Stored'
        activate mq
        deactivate mq
        activate autoQa
        autoQa --) mq: Send 'QA Completed'
        activate mq
        deactivate autoQa
    end

    mq -) backend: Receive 'QA Completed'
    activate backend
    backend ->> backend: Update QA Decision in Metadata
    deactivate backend

    mq -) community: Receive 'QA Completed'
    deactivate mq
    activate community
    community ->> community: Update Data Requests and send notifications
    deactivate community
    
```
