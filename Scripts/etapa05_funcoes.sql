use coffee_n_sweets;

drop function if exists fn_gasto_total_cliente;
drop function if exists fn_categoria_cliente;

delimiter $$

create function fn_gasto_total_cliente(p_id_cliente int)
returns decimal(10, 2)
reads sql data
begin
    declare v_total decimal(10, 2);

    select coalesce(sum(i.Quantidade * i.Preco_unitario), 0.00)
      into v_total
      from Pedido_pagamento p
      join Item_pedido    i on i.ID_pagamento = p.ID_pagamento
     where p.ID_cliente = p_id_cliente;

    return v_total;
end$$

create function fn_categoria_cliente(p_id_cliente int)
returns varchar(20)
reads sql data
begin
    declare v_total decimal(10,2);
    declare v_categoria varchar(20);

    set v_total = fn_gasto_total_cliente(p_id_cliente);

    if v_total >= 50.0 then
        set v_categoria = 'Ouro';
    elseif v_total >= 25.0 then
        set v_categoria = 'Prata';
    elseif v_total > 0.0 then
        set v_categoria = 'Bronze';
    else 
        set v_categoria = 'Novo';
    end if;

    return v_categoria;

end$$


delimiter ;