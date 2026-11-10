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
after insert on Item_pedido
for each row
begin
    update Pedido_pagamento
    set Valor_total = coalesce(Valor_total, 0) + (new.Quantidade * new.Preco_unitario)
    where ID_pagamento = new.ID_pagamento;
end;

create trigger trg_logspreco
after update on Produto
for each row
begin
    if old.Preco != new.Preco then
	    insert into Log_alteracao_preco (ID_produto, Preco_antigo, Preco_novo)
		values (old.ID_produto, old.Preco, new.Preco);
    end if;
end;