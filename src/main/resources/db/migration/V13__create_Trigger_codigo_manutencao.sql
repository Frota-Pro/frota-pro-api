CREATE SEQUENCE IF NOT EXISTS seq_manutencao_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_manutencao()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'MAN-' || LPAD(nextval('seq_manutencao_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE tb_manutencao
    ADD COLUMN codigo varchar(50);


CREATE TRIGGER tg_manutencao_codigo
BEFORE INSERT ON tb_manutencao
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_manutencao();