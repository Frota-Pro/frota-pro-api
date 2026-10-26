SELECT count(*) AS cargas,
       COALESCE(sum(entregas), 0) AS entregas,
       COALESCE(sum(notas), 0) AS notas,
       COALESCE(sum(km), 0) AS km,
       round(sum(combustivel + outras) FILTER (WHERE km IS NOT NULL) / NULLIF(sum(km), 0), 2) AS custo_por_km
FROM cg
