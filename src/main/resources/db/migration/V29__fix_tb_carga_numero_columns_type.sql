-- V29__fix_tb_carga_numero_columns_type.sql
-- Ajusta tb_carga.numero_carga e numero_carga_externo para varchar(50)
-- Funciona tanto se a coluna estiver bytea (converte) quanto se já estiver texto (apenas cast).

DO $$
DECLARE
t_numero_carga regtype;
    t_numero_carga_externo regtype;
BEGIN
SELECT atttypid::regtype
INTO t_numero_carga
FROM pg_attribute
WHERE attrelid = 'public.tb_carga'::regclass
       AND attname  = 'numero_carga'
       AND attnum > 0
       AND NOT attisdropped;

IF t_numero_carga = 'bytea'::regtype THEN
        EXECUTE
            'ALTER TABLE tb_carga ' ||
            'ALTER COLUMN numero_carga TYPE varchar(50) ' ||
            'USING convert_from(numero_carga, ''UTF8'')';
ELSE
        EXECUTE
            'ALTER TABLE tb_carga ' ||
            'ALTER COLUMN numero_carga TYPE varchar(50) ' ||
            'USING numero_carga::varchar(50)';
END IF;

SELECT atttypid::regtype
INTO t_numero_carga_externo
FROM pg_attribute
WHERE attrelid = 'public.tb_carga'::regclass
       AND attname  = 'numero_carga_externo'
       AND attnum > 0
       AND NOT attisdropped;

IF t_numero_carga_externo = 'bytea'::regtype THEN
        EXECUTE
            'ALTER TABLE tb_carga ' ||
            'ALTER COLUMN numero_carga_externo TYPE varchar(50) ' ||
            'USING CASE ' ||
            '  WHEN numero_carga_externo IS NULL THEN NULL ' ||
            '  ELSE convert_from(numero_carga_externo, ''UTF8'') ' ||
            'END';
ELSE
        EXECUTE
            'ALTER TABLE tb_carga ' ||
            'ALTER COLUMN numero_carga_externo TYPE varchar(50) ' ||
            'USING CASE ' ||
            '  WHEN numero_carga_externo IS NULL THEN NULL ' ||
            '  ELSE numero_carga_externo::varchar(50) ' ||
            'END';
END IF;
END $$;