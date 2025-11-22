CREATE SEQUENCE IF NOT EXISTS seq_manutencao_codigo START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_oficina_codigo START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_mecanico_codigo START 1 INCREMENT 1;

CREATE OR REPLACE FUNCTION gerar_codigo_manutencao()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'MAN-' || LPAD(nextval('seq_manutencao_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION gerar_codigo_oficina()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'OFC-' || LPAD(nextval('seq_oficina_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION gerar_codigo_mecanico()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := 'MEC-' || LPAD(nextval('seq_mecanico_codigo')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_manutencao_codigo
BEFORE INSERT ON tb_manutencao
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_manutencao();

CREATE TRIGGER tg_oficina_codigo
BEFORE INSERT ON tb_oficina
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_oficina();

CREATE TRIGGER tg_mecanico_codigo
BEFORE INSERT ON tb_mecanico
FOR EACH ROW
EXECUTE FUNCTION gerar_codigo_mecanico();