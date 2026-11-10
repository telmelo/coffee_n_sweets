use coffee_n_sweets;

create index idx_pedido_cliente_data on Pedido_pagamento (ID_cliente, Data);

create index idx_pedido_data_forma_valor on Pedido_pagamento (Data, Forma_pagamento, Valor_total);