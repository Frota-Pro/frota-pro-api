SELECT rota, count(*) AS cargas
FROM cg
GROUP BY 1
ORDER BY 2 DESC, 1
LIMIT 8
