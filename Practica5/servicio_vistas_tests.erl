-module(servicio_vistas_tests).
-include_lib("eunit/include/eunit.hrl").
-include("sv.hrl").


-compile(export_all).

-define(HOST, '127.0.0.1').

-define(T_ESPERA, 5).



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
comprobar(NodoCliente, NodoPrimario, NodoCopia, NumVista) ->
    {Vista, IsOk} = cliente:obten_vista(NodoCliente),
    ?debugFmt("comprobar VISTA ~p ~p!!!!~n", [Vista, IsOk]),
     P = vista:primario(Vista),
     ?debugFmt("comprobar primario ~p !!!!~n", [P]),
   if  P =/= NodoPrimario ->
            ?debugFmt("Primario esperado ~p, obtenido ~p~n",
                                        [NodoPrimario,P]),
            exit(fin);
        true -> ok
    end,
    
    C = vista:copia(Vista),
    if C =/= NodoCopia ->
            ?debugFmt("Copia esperada ~p, obtenida ~p~n",
                                        [NodoCopia, C]),
            exit(fin);
        true -> ok
    end,
    
    N = vista:num_vista(Vista),
    if N =/= NumVista ->
            ?debugFmt("Nº vista esperado ~p, obtenido ~p~n",
                                        [NumVista, N]),
            exit(fin);
        true -> ok
    end,
    

    PC = cliente:primario(NodoCliente),
    if PC =/= NodoPrimario ->
            ?debugFmt("Primario esperado ~p, obtenido ~p~n",
                                        [NodoPrimario, PC]),
            exit(fin);
        true -> ok
    end.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
tests() ->
    
    %% Poner en marcha el servidor de vistas y 3 clientes
    SV = servidor:start(?HOST, sv),
    C1 = cliente:start(?HOST, c1, SV),
    C2 = cliente:start(?HOST, c2, SV),
    C3 = cliente:start(?HOST, c3, SV),
    
    P = cliente:primario(C1),
    if  P =/= undefined -> exit("Hay un primario demasiado pronto");
        true -> ok
    end,
    
    timer:sleep(?T_ESPERA),
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("1º TEST\n"),
    %% Test 1 : Un primer cliente (C1) es configurado como primer primario
    primer_primario(C1),
    
    %% Test 2 : A continuación, otro cliente se incorpora
    %%          como primer servidor copia (C2)
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("2º TEST\n"),
    primer_nodo_copia(C1, C2),
    
    %% Test 3 : Después, Copia (C2) toma el relevo si Primario falla.
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("3º TEST\n"),
    relevo_copia(C1, C2),
    
    %% Test 4 : Servidor rearrancado (C1) se convierte en copia.
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("4º TEST\n"),
    nodo_copia(C2, C1),
    
    %% Test 5 : 3er servidor en espera (C3) se convierte en copia
    %%          si primario falla.
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("5º TEST\n"),
    espera_a_copia(C2, C1, C3),
    
    %% Test 6 : Primario rearrancado (C2) es tratado como caido.
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("6º TEST\n"),
    rearrancado_caido(C1, C2, C3),
    
    %% Test 7 : Servidor de vistas espera a que primario confirme vista
    %%          pero este no lo hace.
    %%          Poner C3 como Primario, C1 como Copia, C2 para comprobar
    %%          - C3 no confirma vista en que es primario,
    %%          - Cae, pero C1 no es promocionado porque C3 no confimo !
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("7º TEST\n"),
    primario_no_confirma_vista(C1, C2, C3),
    
    %% Test 8 : Si anteriores servidores caen (Primario  y Copia),
    %%       un nuevo servidor sin inicializar no puede convertirse en primario.
    ?debugMsg("--------------------------------------------------------------------------------------------------------------------------\n"),
    ?debugMsg("8º TEST\n"),
    sin_inicializar_no(C1, C2, C3),
    
    
    %N = [1],
    %?debugFmt("Nodo = ~p",[tam(N)]),


    %% parar todos los nodos
    servidor:stop(SV),
    lists:foreach(fun(X) -> cliente:stop(X) end, [C1, C2, C3]).
    % lists:foreach(fun(X) -> cliente:stop(X) end, [C1]).




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Primer test : primer primario
%% Con un parametro
primer_primario(C) ->
    ?debugFmt("Test: Primer primario ...~n",[]),
    
    primer_primario(C, ?PINGS_FALLIDOS * 2),
    comprobar(C, C, undefined, 1),
    
    ?debugFmt("   ...Superado~n",[]).

%% Con 2 parametros
primer_primario(_C, 0) -> fin;
primer_primario(C, X) ->
    if X =< X ->
        {Vista, IsOk} = cliente:ping(C, 0),
        ?debugFmt("primer primario VISTA ~p ~p, X = ~p !!!!~n", [IsOk, Vista, X]),
        Primario = vista:primario(Vista),
        ?debugFmt("PRIMARIO ~p, X = ~p ~n", [Primario, X]),
        if  Primario =/= C -> 
                timer:sleep(?INTERVALO_PING),
                primer_primario(C, X - 1);
            true -> ok
        end
    end.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Segundo test : primer nodo copia
%% Con un parametro
primer_nodo_copia(C1, C2) ->
    ?debugFmt("Test: Primer nodo copia ...~n",[]),
    
    {Vista, _IsOk} = cliente:obten_vista(C1),
    primer_nodo_copia(C1, C2, ?PINGS_FALLIDOS * 2),
    comprobar(C1, C1, C2, vista:num_vista(Vista) +1),
    
    ?debugFmt("   ...Superado~n",[]).

%% Con 2 parametros
primer_nodo_copia(_C1, _C2, 0) -> fin;
primer_nodo_copia(C1, C2, X) ->
    cliente:ping(C1, 1),
    {Vista, _IsOk} = cliente:ping(C2, 0),
    Copia = vista:copia(Vista),
    if  Copia =/= C2 -> 
            timer:sleep(?INTERVALO_PING),
            primer_nodo_copia(C1, C2, X - 1);
        true -> ok
    end.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
relevo_copia(C1, C2) ->
    ?debugFmt("Test: Copia toma Relevo ...~n",[]),
    {Vista, _IsOk} = cliente:obten_vista(C1),
    relevo_copia(C2, ?PINGS_FALLIDOS * 2,vista:num_vista(Vista)),
    ?debugFmt("   ...Hecho~n",[]),
    {Vista2, _IsOk} = cliente:ping(C2, vista:num_vista(Vista) +1),
    comprobar(C2, C2, undefined, vista:num_vista(Vista2)),
    ?debugFmt("   ...Superado~n",[]).


%% Con 2 parametros
relevo_copia(_C2, 0,NumVista) -> fin;
relevo_copia(C2, X, NumVista) ->
    %cliente:ping(C1, 1),
    {Vista, _IsOk} = cliente:ping(C2, NumVista),
    ?debugFmt("   ...Enviado~n",[]),
    Primario = vista:primario(Vista),
    if  Primario =/= C2 -> 
            timer:sleep(?INTERVALO_PING),
            relevo_copia(C2, X - 1,NumVista);
        true -> ok
    end.
    
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

nodo_copia(C1, C2) ->
    ?debugFmt("Test: Nodo copia ...~n",[]),

    {Vista, _IsOk} = cliente:obten_vista(C1),
    nodo_copia(C1,C2, ?PINGS_FALLIDOS * 2,vista:num_vista(Vista)),
    comprobar(C1, C1, C2, vista:num_vista(Vista) +1),
    
    ?debugFmt("   ...Superado~n",[]).

%% Con 2 parametros
nodo_copia(_C1,_C2, 0,NumVista) -> fin;
nodo_copia(C1, C2, X,NumVista) ->
    cliente:ping(C1,NumVista),
    {Vista, _IsOk} = cliente:ping(C2, 0),
    Copia = vista:copia(Vista),
    if  Copia =/= C2 -> 
            timer:sleep(?INTERVALO_PING),
            nodo_copia(C1, C2, X - 1,NumVista);
        true -> ok
    end.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

espera_a_copia(C1, C2, C3) ->
    ?debugFmt("Test: Espera a copia ...~n",[]),
    
    {Vista, _IsOk} = cliente:obten_vista(C1),
    cliente:ping(C3, 0),
    nodo_copia(C2,C3, ?PINGS_FALLIDOS * 2,vista:num_vista(Vista)),
    {Vista2, _IsOk} = cliente:ping(C2, vista:num_vista(Vista) +1),
    comprobar(C1, C2, C3, vista:num_vista(Vista2)),
    
    ?debugFmt("   ...Superado~n",[]).

%% Con 2 parametros
espera_a_copia(_C2,_C3, 0,NumVista) -> fin;
espera_a_copia(C2, C3, X,NumVista) ->
    cliente:ping(C2,NumVista),
    {Vista, _IsOk} = cliente:ping(C3, NumVista),
    Copia = vista:copia(Vista),
    if  Copia =/= C3 -> 
            timer:sleep(?INTERVALO_PING),
            nodo_copia(C2, C3, X - 1,NumVista);
        true -> ok
    end.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


rearrancado_caido(C1, C2, C3) ->
    ?debugFmt("Test: rearrancado caido ...~n",[]),
    
    %{Vista, _IsOk} = cliente:obten_vista(C1),
    {Vista, _IsOk} = cliente:ping(C2, 0),
    %nodo_copia(C,C3, ?PINGS_FALLIDOS * 2,vista:num_vista(Vista)),
    %{Vista2, _IsOk} = cliente:ping(C2, vista:num_vista(Vista) +1),
    comprobar(C1, C1, C3, vista:num_vista(Vista)),
    
    ?debugFmt("   ...Superado~n",[]).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


primario_no_confirma_vista(C1, C2, C3) ->
	    ?debugFmt("Test: primario_no_confirma_vista ...~n",[]),

    {Vista, _IsOk} = cliente:obten_vista(C3),
    {Vista2, _IsOk} = cliente:ping(C3,vista:num_vista(Vista)),
    {Vista3, _IsOk} = cliente:ping(C3,vista:num_vista(Vista2)),
    timer:sleep(?INTERVALO_PING * 20),
    comprobar(C1,C1,C3,vista:num_vista(Vista)),
    ?debugFmt("   ...Superado~n",[]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


sin_inicializar_no(C1,C2,C3) ->
    ?debugFmt("Test: rearrancado caido ...~n",[]),
    {Vista, _IsOk} = cliente:obten_vista(C3),
    %?debugFmt("   ...Superadodasfasdf~n",[Vista]),
    %{Vista2, _IsOk} = cliente:ping(C3,vista:num_vista(Vista)),
    %{Vista3, _IsOk} = cliente:ping(C3,vista:num_vista(Vista2)),
    timer:sleep(?INTERVALO_PING * 10),
    cliente:ping(C1,1),
    comprobar(C1,C1,C3,vista:num_vista(Vista)).

%%%%%%%%%%%%%%%%%%%%%% GENERADORES DE TEST %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 
pruebas_test_() ->
    { spawn, { timeout, 6, ?_test(tests()) } }.
