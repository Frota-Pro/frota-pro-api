WITH abc AS (SELECT p.carga_id, sum(a.qt_litros) litros, sum(a.valor_total) FILTER (WHERE NOT COALESCE(a.preco_anomalo, false)) comb FROM tb_abastecimento a JOIN tb_parada_carga p ON p.id = a.parada_id GROUP BY 1),
dpc AS (SELECT p.carga_id, sum(d.valor) FILTER (WHERE d.tipo_despesa <> 'COMBUSTIVEL') outras FROM tb_despesa_parada d JOIN tb_parada_carga p ON p.id = d.parada_id GROUP BY 1),
en AS (SELECT carga_id, count(*) n FROM tb_carga_ordem_entrega GROUP BY 1),
nf AS (SELECT carga_id, count(*) n FROM tb_carga_nota GROUP BY 1),
cg AS (SELECT c.numero_carga carga, COALESCE(c.data_saida, c.data_faturamento) data, c.status_carga status, k.placa, COALESCE(cc.descricao, 'Sem categoria') categoria, mo.nome motorista, r.codigo || ' · ' || r.cidade_inicio rota,
  COALESCE(en.n, 0) entregas, COALESCE(nf.n, 0) notas, c.peso_carga peso_t,
  CASE WHEN c.km_final - c.km_inicial BETWEEN 1 AND 3000 THEN c.km_final - c.km_inicial END km,
  COALESCE(abc.litros, 0) litros, COALESCE(abc.comb, 0) combustivel, COALESCE(dpc.outras, 0) outras
  FROM tb_carga c JOIN tb_caminhao k ON k.id = c.caminhao_id LEFT JOIN tb_categoria_caminhao cc ON cc.id = k.categoria_caminhao_id
  JOIN tb_motorista mo ON mo.id = c.motorista_id JOIN tb_rota r ON r.id = c.rota_id
  LEFT JOIN abc ON abc.carga_id = c.id LEFT JOIN dpc ON dpc.carga_id = c.id LEFT JOIN en ON en.carga_id = c.id LEFT JOIN nf ON nf.carga_id = c.id
  WHERE 1 = 1 AND COALESCE(c.data_saida, c.data_faturamento) >= current_date - make_interval(months => :meses))
