#!/usr/bin/env bash
set -euo pipefail

#Configuration for the migrateData.sh script

export SOURCE=
export SOURCE_TOKEN=
export TARGET=
export TARGET_TOKEN=
export ONLY_ACTIVE=false
#to migrate all frameworks use: eutaxonomy-financials,eutaxonomy-non-financials,lksg,sfdr,p2p,sme
export FRAMEWORKS=eutaxonomy-financials,eutaxonomy-non-financials,lksg,sfdr,p2p,sme