ALTER TABLE tb_carga_nota
    ADD COLUMN arquivo_id uuid REFERENCES tb_arquivo (id);

COMMENT ON COLUMN tb_carga_nota.arquivo_id IS
    'XML da nota fiscal que originou esta linha, quando cadastrada manualmente via upload (import de NFe) em vez de sincronizada do WinThor. Nulo para notas vindas da integracao.';
