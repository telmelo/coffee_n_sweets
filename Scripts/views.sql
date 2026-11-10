use coffee_n_sweets;

create view vw_vendas_detalhadas as
    select ped_pag.ID_pagamento,
    itm_ped.Numero_item,
    ped_pag.Data as Data_pedido, ped_pag.Horario as Horario_pedido,
    ped_pag.Tipo_consumo, ped_pag.Forma_pagamento,
    cli.Nome as Nome_cliente,
    fun.Nome as Nome_atendente,
    coalesce(cast(mesa.Numero_mesa as char), 'Para viagem') as Mesa,
    prod.Nome as Nome_produto,
    itm_ped.Quantidade as Quantidade_item,
    itm_ped.Preco_unitario as Preco_unitario,
    (itm_ped.Quantidade * itm_ped.Preco_unitario) as Subtotal_item
    from Pedido_pagamento ped_pag
    join Cliente cli on cli.ID_cliente = ped_pag.ID_cliente
    join Funcionario fun on fun.ID_funcionario = ped_pag.ID_funcionario
    left join Mesa mesa on mesa.Numero_mesa = ped_pag.Numero_mesa
    join Item_pedido itm_ped on itm_ped.ID_pagamento = ped_pag.ID_pagamento
    join Produto prod on prod.ID_produto = itm_ped.ID_produto;

create view vw_producao_barista_equipamento as
    select fun.ID_funcionario, fun.Nome as Nome_barista,
    eq.ID_equipamento, eq.Nome as Nome_equipamento,
    count(distinct itm_ped.Numero_item, itm_ped.ID_pagamento) as Itens_distintos_preparados,
    sum(itm_ped.Quantidade) as Unidades_produzidas,
    count(distinct itm_ped.ID_produto) as Produtos_diferentes,
    min(ped_pag.Data) as Primeiro_preparo, max(ped_pag.Data) as Ultimo_preparo
    from Funcionario fun
    join Prepara prep on prep.ID_funcionario = fun.ID_funcionario
    join Equipamento eq on eq.ID_equipamento = prep.ID_equipamento
    join Item_pedido itm_ped on itm_ped.Numero_item = prep.Numero_item
    and itm_ped.ID_pagamento = prep.ID_pagamento
    join Pedido_pagamento ped_pag on ped_pag.ID_pagamento = itm_ped.ID_pagamento
    where fun.Cargo = 'Barista'
    group by fun.ID_funcionario, fun.Nome, eq.ID_equipamento, eq.Nome;