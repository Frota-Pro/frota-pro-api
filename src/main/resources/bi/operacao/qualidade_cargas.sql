SELECT count(*) FILTER (WHERE origem = 'REENTREGA') AS reentregas,
       count(*) FILTER (WHERE origem = 'TRANSFERENCIA') AS transferencias,
       count(*) FILTER (WHERE origem = 'MANIFESTO_SEM_NOTA') AS manifestos_sem_nota,
       count(*) FILTER (WHERE terceiro) AS motoristas_terceiro,
       count(*) FILTER (WHERE peso_t > 0 AND peso_t < 0.5) AS cargas_pequenas,
       count(*) FILTER (WHERE (terceiro OR origem = 'MANIFESTO_SEM_NOTA') AND km IS NULL) AS lancar_km
FROM cg
