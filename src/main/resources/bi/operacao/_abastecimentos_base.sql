WITH ab AS (SELECT a0.dt_abastecimento::date data, a0.qt_litros litros, CASE WHEN COALESCE(a0.preco_anomalo, false) THEN 0 ELSE a0.valor_total END valor, COALESCE(a0.preco_anomalo, false) anomalo
  FROM tb_abastecimento a0 JOIN tb_caminhao k ON k.id = a0.caminhao_id LEFT JOIN tb_categoria_caminhao cc ON cc.id = k.categoria_caminhao_id JOIN tb_motorista mo ON mo.id = a0.motorista_id
  WHERE 1 = 1 AND a0.dt_abastecimento::date >= current_date - make_interval(months => :meses))
