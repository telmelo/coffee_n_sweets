use coffee_n_sweets;

create table Log_alteracao_preco (
     ID_log int auto_increment primary key,
     ID_produto int not null,
     Preco_antigo decimal(10, 2) not null,
     Preco_novo decimal(10, 2) not null,
     Data_alteracao datetime not null default current_timestamp,
     Usuario varchar(100) not null default (user()),
     constraint FK_log_produto foreign key (ID_produto) references Produto (ID_produto) on update cascade on delete cascade
);

create trigger trg_valortotal
    AFTER INSERT ON Item_pedido
    FOR EACH ROW
BEGIN
    UPDATE Pedido_pagamento
    SET Valor_total = (
        SELECT COALESCE(SUM(Quantidade * Preco_unitario), 0)
        FROM Item_pedido
        WHERE ID_pagamento = NEW.ID_pagamento
    )
    WHERE ID_pagamento = NEW.ID_pagamento;
END;

create trigger trg_logspreco
after update on Produto
for each row
begin
    if old.Preco != new.Preco then
	    insert into Log_alteracao_preco (ID_produto, Preco_antigo, Preco_novo)
		values (old.ID_produto, old.Preco, new.Preco);
    end if;
end;


CREATE TRIGGER trg_propagar_preco
    AFTER UPDATE ON Produto
    FOR EACH ROW
BEGIN
    IF OLD.Preco <> NEW.Preco THEN
    UPDATE Item_pedido i
        JOIN Pedido_pagamento p ON p.ID_pagamento = i.ID_pagamento
        SET i.Preco_unitario = NEW.Preco
    WHERE i.ID_produto = NEW.ID_produto
      AND p.Status IN ('Pendente', 'Em preparo');

    UPDATE Pedido_pagamento p
    SET p.Valor_total = (
        SELECT COALESCE(SUM(i.Quantidade * i.Preco_unitario), 0)
        FROM Item_pedido i
        WHERE i.ID_pagamento = p.ID_pagamento
    )
    WHERE p.Status IN ('Pendente', 'Em preparo')
      AND p.ID_pagamento IN (SELECT ID_pagamento FROM Item_pedido
                             WHERE ID_produto = NEW.ID_produto);
END IF;
END;

CREATE TRIGGER trg_valortotal_deletar
    AFTER DELETE ON Item_pedido
    FOR EACH ROW
BEGIN
    UPDATE Pedido_pagamento
    SET Valor_total = (
        SELECT COALESCE(SUM(Quantidade * Preco_unitario), 0)
        FROM Item_pedido
        WHERE ID_pagamento = OLD.ID_pagamento
    )
    WHERE ID_pagamento = OLD.ID_pagamento;
END;


