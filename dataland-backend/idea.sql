WITH pre_filtered AS (
    SELECT company_id, company_name
    FROM stored_companies
    WHERE
            ABS(LENGTH(company_name) - LENGTH('berlin')) <= 2
       OR
        company_name ILIKE '%berlin%'
    )
SELECT
    pre_filtered.company_id,
    MAX(pre_filtered.company_name) AS company_name,
    MAX(
            CASE
                WHEN company_name = 'berlin' THEN 10
                WHEN company_name ILIKE 'berlin' THEN 5
                WHEN levenshtein(company_name, 'berlin') < 3 THEN 3
                ELSE 1
                END
        ) AS match_quality,
    MAX(
            CASE
                WHEN data_id IS NOT NULL THEN 2
                ELSE 1
                END
        ) AS dataset_rank
FROM
    pre_filtered
        LEFT JOIN
    data_meta_information ON pre_filtered.company_id = data_meta_information.company_id AND currently_active = true
WHERE
   --levenshtein(company_name, 'berlin') < 3
        levenshtein_less_equal(company_name, 'berlin', 2) < 3
   OR
    company_name ILIKE '%berlin%'
GROUP BY
    pre_filtered.company_id
