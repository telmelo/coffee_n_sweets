use coffee_n_sweets;

select p.ID_produto, p.Nome, p.Preco from Produto p
left join Item_pedido ip on ip.ID_produto = p.ID_produto
where ip.ID_produto is null order by p.Nome;

select m.Numero_mesa, m.Localizacao, pp.ID_pagamento, pp.Data, pp.Valor_total from Mesa m
left join Pedido_pagamento pp on pp.Numero_mesa = m.Numero_mesa
union
select m.Numero_mesa, m.Localizacao, pp.ID_pagamento, pp.Data, pp.Valor_total from Mesa m
right join Pedido_pagamento pp on pp.Numero_mesa = m.Numero_mesa
order by Numero_mesa, ID_pagamento;

select c.ID_cliente, c.Nome, pp.ID_pagamento, pp.Data as Data_ultimo_pedido, pp.Forma_pagamento, pp.Valor_total,
datediff(curdate(), pp.Data) as Dias_sem_comprar from Pedido_pagamento pp
join Cliente c on c.ID_cliente = pp.ID_cliente
where pp.Data = (
    select max(pp2.Data) from Pedido_pagamento pp2 where pp2.ID_cliente = pp.ID_cliente
    )
order by Dias_sem_comprar desc;

select c.ID_cliente, c.Nome, c.Email from Cliente c
where exists (
    select 1 from Pedido_pagamento pp
    join Item_pedido ip on ip.ID_pagamento = pp.ID_pagamento
    join Cafe ca on ca.ID_produto = ip.ID_produto
    where pp.ID_cliente = c.ID_cliente
)
and not exists (
    select 1
    from Pedido_pagamento pp
    join Item_pedido ip on ip.ID_pagamento = pp.ID_pagamento
    join Lanche la on la.ID_produto = ip.ID_produto
    where pp.ID_cliente = c.ID_cliente
)
order by c.Nome;