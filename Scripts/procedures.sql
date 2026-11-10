use coffee_n_sweets;

drop procedure if exists sp_atualizar_status_pedido;
drop procedure if exists sp_distribuir_itens_pendentes;

create procedure sp_atualizar_status_pedido(
    in  p_id_pagamento int,
    in  p_novo_status  varchar(40),
    out p_mensagem     varchar(100)
)
begin
    declare v_status_atual varchar(40);

    select Status
    into v_status_atual
    from Pedido_pagamento
    where ID_pagamento = p_id_pagamento;

    if v_status_atual is null then
        set p_mensagem = 'Pedido não encontrado';
    elseif (v_status_atual = 'Pendente'   and p_novo_status = 'Em preparo')
        or (v_status_atual = 'Em preparo' and p_novo_status = 'Pronto') then
        update Pedido_pagamento
        set Status = p_novo_status
        where ID_pagamento = p_id_pagamento;
        set p_mensagem = concat('Status alterado de ', v_status_atual, ' para ', p_novo_status);
    else
        set p_mensagem = concat('Transição inválida: ', v_status_atual, ' para ', p_novo_status);
    end if;
end;

create procedure sp_distribuir_itens_pendentes(out p_itens_atribuidos int)
begin
    declare done int default false;
    declare v_numero_item int;
    declare v_id_pagamento int;
    declare v_id_produto  int;
    declare v_barista     int;
    declare v_equipamento int;
    declare v_padrao_equip varchar(40);

    declare cur_itens cursor for
        select i.Numero_item, i.ID_pagamento, i.ID_produto
        from Item_pedido i
        join Pedido_pagamento p on p.ID_pagamento = i.ID_pagamento
        where p.Status = 'Pendente'
        and not exists (select 1
                    from Prepara pr
                    where pr.Numero_item  = i.Numero_item
                    and pr.ID_pagamento = i.ID_pagamento)
        order by p.Data, p.Horario, i.Numero_item;

    declare continue handler for not found set done = true;

    set p_itens_atribuidos = 0;

    open cur_itens;

    cursor_loop: loop
        fetch cur_itens into v_numero_item, v_id_pagamento, v_id_produto;
        if done then
            leave cursor_loop;
        end if;

        select f.ID_funcionario
        into v_barista
        from Funcionario f
        left join Prepara pr on pr.ID_funcionario = f.ID_funcionario
        where f.Cargo = 'Barista'
        group by f.ID_funcionario
        order by count(pr.Numero_item), f.ID_funcionario
        limit 1;

        if exists (select 1 from Cafe where ID_produto = v_id_produto) then
            set v_padrao_equip = 'Máquina de Espresso%';
        elseif exists (select 1 from Lanche where ID_produto = v_id_produto) then
            set v_padrao_equip = 'Forno Elétrico%';
        else
            set v_padrao_equip = 'Liquidificador%';
        end if;

        select e.ID_equipamento
        into v_equipamento
        from Equipamento e
        left join Prepara pr on pr.ID_equipamento = e.ID_equipamento
        where e.Nome like v_padrao_equip
        group by e.ID_equipamento
        order by count(pr.ID_equipamento), e.ID_equipamento
        limit 1;

        insert into Prepara (Numero_item, ID_pagamento, ID_funcionario, ID_equipamento)
        values (v_numero_item, v_id_pagamento, v_barista, v_equipamento);

        set p_itens_atribuidos = p_itens_atribuidos + 1;
    end loop cursor_loop;

    close cur_itens;

    update Pedido_pagamento p
    set p.Status = 'Em preparo'
    where p.Status = 'Pendente'
    and exists (select 1 from Prepara pr where pr.ID_pagamento = p.ID_pagamento);
end;