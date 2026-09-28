use coffee_n_sweets;

select Data, SUM(Valor_total) as Faturamento, Forma_pagamento from Pedido_pagamento group by Data, Forma_pagamento;

select p.ID_produto, p.Nome, SUM(i.Quantidade) as Total_vendido from Item_pedido i
join Produto p on i.ID_produto = p.ID_produto group by p.ID_produto, p.Nome order by Total_vendido desc limit 10;

select f.ID_funcionario, f.Nome, COUNT(distinct p.Numero_item, p.ID_pagamento) as Itens_preparados from Prepara p
join Funcionario f on p.ID_funcionario = f.ID_funcionario where f.cargo = 'Barista' group by f.ID_funcionario, f.Nome having Itens_preparados > (
	select AVG(Itens_preparados2) from (
		select COUNT(distinct p2.Numero_item, p2.ID_pagamento) as Itens_preparados2 from Prepara p2
		join Funcionario f2 on p2.ID_funcionario = f2.ID_funcionario where f2.cargo = 'Barista' group by f2.ID_funcionario
	) as Media_preparo
);

select p.Nome as Nome_cafe, g.Nome as Nome_grao, g.Origem, fo.Razao_social, fn.Preco_kg from Cafe c join Grao g on c.ID_grao = g.ID_grao join Fornece fn on g.ID_grao = fn.ID_grao
join Fornecedor fo on fn.ID_fornecedor = fo.ID_fornecedor join Produto p on c.ID_produto = p.ID_produto order by p.Nome;


