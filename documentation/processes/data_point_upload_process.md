# Dataland Data Upload And QA Process (Technical)
| Metadata        | Value                                                                                                                                                                                                                               |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Target Audience | Dataland Developers                                                                                                                                                                                                                 |
| Abstract        | This document describes the process of uploading data points to Dataland                                                                                                                                                            |


The following diagram describes the process of uploading a data point to Dataland. It focuses on any messages that are sent via the message-queue.
Please note that all message-queue events are asynchronous and are not necessarily in the order they are shown in the diagram.

```mermaid
sequenceDiagram
    actor Uploader
    participant mq as Message Queue
    participant backend as Dataland Backend
    participant storage as Internal Storage
    participant qaService as QA Service
    actor qaler as QA Provider

    Uploader ->> backend: Upload Data Point
    activate backend
    backend ->> backend: Store Meta-Data
    backend ->> backend: Store Data Point in Temporary Storage
    backend --) mq: Send 'Public Data received'
    activate mq
    backend -->> Uploader: Upload completed
    deactivate backend

    mq -) storage: Receive 'Public Data received'
    deactivate mq
    activate storage
    storage ->> backend: Get Data Point from Temporary Storage
    activate backend
    backend -->> storage: Data Point
    deactivate backend
    storage ->> storage: Store Data Point in Database
    storage --) mq: Send 'Item Stored'
    activate mq
    deactivate storage

    mq -) backend: Receive 'Item Stored'
    activate backend
    backend ->> backend: Remove Data Point from Temporary Storage
    deactivate backend
    mq -) qaService: Receive 'Public Data received'
    deactivate mq
    activate qaService

    opt bypassQa is false
        qaService ->> qaService: Store Pending entry in QA-DB table
        deactivate qaService

        qaler ->> qaService: Get Data Points to QA
        activate qaService
        activate qaler
        qaService -->> qaler: Data Point List
        deactivate qaService
        qaler ->> qaService: Quality Assured
        deactivate qaler
        activate qaService
    end

    qaService ->> qaService: Store decision in QA-DB table

    qaService --) mq: Send 'QA status updated'
    deactivate qaService
    activate mq

    mq -) backend: Receive 'QA status updated'
    activate backend
    backend ->> backend: Update QA Decision in Metadata
    deactivate backend
```