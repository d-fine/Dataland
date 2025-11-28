package org.dataland.datalandaccountingservice.repositories.utils

object JPQLQueryFragments {
    const val BILLED_DATA_SOURCING_IDS_FOR_MEMBER_TO_BILL =
        "SELECT e2.dataSourcingId FROM BilledRequestEntity e2 WHERE e2.billedCompanyId = :billedCompanyId"

    const val REQUESTING_MEMBER_NUMBERS_PER_BILLED_DATA_SOURCING_FOR_MEMBER_TO_BILL =
        "SELECT COUNT(e) AS number_of_requesting_members " +
            "FROM BilledRequestEntity e WHERE e.dataSourcingId IN ($BILLED_DATA_SOURCING_IDS_FOR_MEMBER_TO_BILL) GROUP BY e.dataSourcingId"

    const val CREDIT_DEBTS_FROM_BILLED_REQUESTS_FOR_MEMBER_TO_BILL =
        "SELECT (CASE " +
            "WHEN number_of_requesting_members = 1 THEN 1.0 " +
            "WHEN number_of_requesting_members = 2 THEN 0.5 " +
            "WHEN number_of_requesting_members = 3 THEN 0.4 " +
            "WHEN number_of_requesting_members = 4 THEN 0.3 " +
            "WHEN number_of_requesting_members <= 9 THEN 0.2 " +
            "ELSE 0.1 " +
            "END) AS credit_debts FROM ($REQUESTING_MEMBER_NUMBERS_PER_BILLED_DATA_SOURCING_FOR_MEMBER_TO_BILL)"
}
