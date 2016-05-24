%% ----------------------------------------------------------------------------
%% servidor : Modulo servidor de vistas
%%
%% ----------------------------------------------------------------------------

-module(servidor).
-include("sv.hrl").
-include_lib("eunit/include/eunit.hrl").

-export([start/2, stop/1]).

-export([init_sv/0, init_monitor/0]).



 %% Registro que guarda el estado del servidor de vistas
-record(estado_sv, {vista_valida = vista:vista_inicial(),
			vista_tentativa = vista:vista_inicial(),
			primario_fallidos :: integer(),
			primario_ping :: integer(),
			copia_fallidos :: integer(),
			copia_ping :: integer(),
			espera = []
                    }
        ).
-record(nd, {nodo = node(),pings_fallidos :: integer(),ping :: integer()}).


%%%%%%%%%%%%%%%%%%%% Interface (FUNCIONES EXPORTABLES)  %%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Poner en marcha el servicio de vistas con 2 procesos concurrentes
-spec start( atom(), atom() ) -> node().
start(Host, NombreNodo) ->
   % ?debugFmt("Arrancar un nodo servidor vistas~n",[]),
    %%%%% VUESTRO CODIGO DE INICIALIZACION
    
     % args para comando remoto erl
    Args = "-connect_all false -setcookie palabrasecreta",
        % arranca servidor en nodo remoto
    {ok, Nodo} = slave:start(Host, NombreNodo, Args),
  %  ?debugFmt("Nodo servidor vistas en marcha : ~p~n",[Nodo]),
    process_flag(trap_exit, true),
    spawn_link(Nodo, ?MODULE, init_sv, []),
    Nodo.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Parar nodo servidor de vista al completo, includos los 2 procesos
-spec stop( atom() ) -> ok.
stop(Nodo) ->
    slave:stop(Nodo),
    timer:sleep(10),
    comun:vaciar_buzon(),
    ok.
    


%%------------------------  FUNCIONES LOCALES  ------------------------------%%


%%-----------------------------------------------------------------------------
init_sv() ->
    register(sv, self()),
    spawn_link(?MODULE, init_monitor, []),
	%Comprobar mis maquinas, nombrar una primaria y otra back up y crear la primera vista
    %algo(1),
    SV = #estado_sv{vista_valida = vista:vista_inicial(), vista_tentativa = vista:vista_inicial(),
	primario_fallidos = 0, primario_ping = 0, copia_fallidos = 0, copia_ping = 0, espera = []},
    bucle_recepcion(SV).
    
%%-----------------------------------------------------------------------------
bucle_recepcion(SV) ->
    receive
        {ping, NodoOrigen, NumVista} ->

		%?debugMsg("Recibido\n"),
		Primario = vista:primario(SV#estado_sv.vista_tentativa),
		Copia = vista:copia(SV#estado_sv.vista_tentativa),
		PrimarioValido = vista:primario(SV#estado_sv.vista_valida),
		NumVistaTentativa = vista:num_vista(SV#estado_sv.vista_tentativa),
		if Primario == PrimarioValido orelse (Primario /= PrimarioValido andalso Primario == NodoOrigen
				andalso NumVista == NumVistaTentativa) ->
			%Se actualiza el numero de pings
			if Primario == NodoOrigen ->
				if NumVista == 0 ->
					if Copia /= undefined ->
						SV1 = nuevo_espera(SV,Primario),
						SV2 = copia_a_primario(SV1),
						SV3 = incrementar_vista(SV2),
						SVP = espera_a_copia(SV3);
					   true ->
						SV1 = SV#estado_sv{primario_ping=1,primario_fallidos=0},
						SVP = actualizar_vista(SV1)
					end;
				   NumVista == NumVistaTentativa ->
					SV1 = actualizar_vista(SV),
					SVP = SV1#estado_sv{primario_ping=1,primario_fallidos=0};
				   true ->
					SVP = SV#estado_sv{primario_ping=1,primario_fallidos=0}
				end;
			   Copia == NodoOrigen ->
				SVP = SV#estado_sv{copia_ping=1,copia_fallidos=0};
			   true ->
			   	case pertenece(NodoOrigen,SV#estado_sv.espera) of
					true -> 
						SVP = SV#estado_sv{espera=actualizar(NodoOrigen,SV#estado_sv.espera)};
					false -> 
						if 
						   NumVista == 0 ->
							%?debugFmt("Añadido a espera= ~p",[NodoOrigen]),
							SVP = nuevo_espera(SV,NodoOrigen);
						   true ->
							SVP = SV
						end
				end
			end,
			PrimarioSVP = vista:primario(SVP#estado_sv.vista_tentativa),
			CopiaSVP = vista:copia(SVP#estado_sv.vista_tentativa),
			if 
			   PrimarioSVP == undefined andalso NumVista == 0 ->
				SVP1 = espera_a_copia(SVP),
				SVP2 = copia_a_primario(SVP1),
				SVP3 = incrementar_vista(SVP2),
				NuevoPrimario = vista:primario(SVP3#estado_sv.vista_tentativa),
				if NuevoPrimario == NodoOrigen ->
					SVF = actualizar_vista(SVP3);
				   true ->
					SVF = SVP3
				end;
			   CopiaSVP == undefined andalso PrimarioSVP /= NodoOrigen andalso NumVista == 0 ->
				SVP1 = espera_a_copia(SVP),
				SVP2 = incrementar_vista(SVP1),
				SVF = actualizar_vista(SVP2);
			   true ->
				SVF = SVP
			end;
		   true ->
			SVF = SV
		end,
		%?debugFmt("Primario ~p",[vista:primario(SVF#estado_sv.vista_tentativa)]),
		%?debugFmt("Copia ~p",[vista:copia(SVF#estado_sv.vista_tentativa)]),
		%?debugFmt("Num final ~p",[vista:num_vista(SVF#estado_sv.vista_tentativa)]),
		%?debugFmt("Espera = ~p",[SVF#estado_sv.espera]),	
		{cliente, NodoOrigen} ! {vista_tentativa,SVF#estado_sv.vista_tentativa,true},
		bucle_recepcion(SVF);
		
        {obten_vista, Pid} -> 
		%?debugMsg("OBTEN VISTA\n"),
		Pid ! {vista_valida,SV#estado_sv.vista_valida},
		bucle_recepcion(SV);
       
        procesa_situacion_servidores ->
		%?debugMsg("procesa_situacion_servidores\n"),
		procesar_situacion_servidores(SV)
    end.

%%-----------------------------------------------------------------------------
init_monitor() ->
    sv ! procesa_situacion_servidores,
    timer:sleep(?INTERVALO_PING),
    init_monitor().
%%----------------------------------------------------------------------------- 
procesar_situacion_servidores(SV) ->
		%controlar pings de los nodos en espera
		SV1 = SV#estado_sv{espera = procesar_espera(SV#estado_sv.espera)},
		Copia = vista:copia(SV#estado_sv.vista_tentativa),
		Primario = vista:primario(SV#estado_sv.vista_tentativa),
		if SV1#estado_sv.copia_ping == 0 andalso Copia =/= undefined ->
			if SV1#estado_sv.copia_fallidos == 4 ->
				SV2 = espera_a_copia(SV1),
				Modificada1 = 1;
			   true ->
				SV2 = SV1#estado_sv{copia_fallidos = SV1#estado_sv.copia_fallidos+1},
				Modificada1 = 0
			end;
		   true ->
			SV2 = SV1#estado_sv{copia_fallidos = 1,copia_ping = 0},
			Modificada1 = 0
		end,
		if SV2#estado_sv.primario_ping == 0 andalso Primario /= undefined ->
			%?debugMsg("No Responde\n"),
			if SV2#estado_sv.primario_fallidos == 4 ->
				SV3 = copia_a_primario(SV2),
				SVF = espera_a_copia(SV3),
				Modificada2 = 1;
			   true ->
				SVF = SV2#estado_sv{primario_fallidos = SV1#estado_sv.primario_fallidos+1},
				Modificada2 = 0
			end;
		   true ->
			SVF = SV2#estado_sv{primario_fallidos = 1,primario_ping = 0},
			Modificada2 = 0
		end,

		if Modificada1 == 1 orelse Modificada2 == 1 ->
			bucle_recepcion(incrementar_vista(SVF));
		   true ->
			bucle_recepcion(SVF)
		end.

		
%%----------------------------------------------------------------------------- 
copia_a_primario(SV) ->
	SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa),
		vista:copia(SV#estado_sv.vista_tentativa),undefined),primario_fallidos = SV#estado_sv.copia_fallidos,copia_fallidos = 0,
		primario_ping=SV#estado_sv.copia_ping,copia_ping=0}.
%%----------------------------------------------------------------------------- 
espera_a_copia(SV) ->
	if SV#estado_sv.espera == [] ->
		SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa),
		vista:primario(SV#estado_sv.vista_tentativa),undefined),copia_fallidos = 0,copia_ping=0};
	   true ->
		SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa),
		vista:primario(SV#estado_sv.vista_tentativa),(espera1(SV#estado_sv.espera))#nd.nodo),
		copia_fallidos = (espera1(SV#estado_sv.espera))#nd.pings_fallidos,copia_ping=(espera1(SV#estado_sv.espera))#nd.ping,
		espera=cola(SV#estado_sv.espera)}
	end.
%%-----------------------------------------------------------------------------
nuevo_primario(SV,NODO) ->
	SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa),
	NODO,vista:copia(SV#estado_sv.vista_tentativa)),primario_fallidos = 0,primario_ping=1}.
%%-----------------------------------------------------------------------------
nuevo_copia(SV,NODO) ->
	SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa),
	vista:primario(SV#estado_sv.vista_tentativa),NODO),copia_fallidos = 0,copia_ping=1}.
%%-----------------------------------------------------------------------------
nuevo_espera(SV,NODO) -> SV#estado_sv{espera = SV#estado_sv.espera ++ [nuevo_nodo(NODO,0,1)]}.
%%----------------------------------------------------------------------------- 
incrementar_vista(SV) ->
	SV#estado_sv{vista_tentativa = vista:nueva_vista(vista:num_vista(SV#estado_sv.vista_tentativa) + 1,
	vista:primario(SV#estado_sv.vista_tentativa),vista:copia(SV#estado_sv.vista_tentativa))}.
%%----------------------------------------------------------------------------- 
actualizar_vista(SV) ->
	SV#estado_sv{vista_valida = SV#estado_sv.vista_tentativa}.
%%----------------------------------------------------------------------------- 
igual(X,Y) ->
	if X == Y ->
		X;
	   true ->
		0
	end.
%%-----------------------------------------------------------------------------
espera1(L) -> espera(L,0).
%%-----------------------------------------------------------------------------
espera(L,N) ->
case L of
	[] -> no;
	[X|T] -> 
		if N == 0 ->
			X;
		   true ->
			espera(T,N-1)
		end
end.
%%-----------------------------------------------------------------------------
cola(L) ->
case L of
	[] -> [];
	[_|T] -> T
end.

%%-----------------------------------------------------------------------------
pertenece(X,L) -> % función pertenencia de X a lista L
case L of
	[]    ->	false;
	[H|T] ->	
		if H#nd.nodo == X ->
			true;
		   true ->
			pertenece(X, T)
		end
end.	
%%-----------------------------------------------------------------------------
actualizar(X,L) -> % función pertenencia de X a lista L
case L of
	[]    ->	[];
	[H|T] ->	
		if H#nd.nodo == X ->
			[H#nd{ping = 1,pings_fallidos = 0}] ++ actualizar(X,T);
		   true ->
			[H] ++ actualizar(X, T)
		end
end.
%%-----------------------------------------------------------------------------
procesar_espera(L) ->
case L of
	[] -> [];
	%[H] -> [];
	[H|T] -> 
		if H#nd.ping == 0 ->
			%?debugMsg("CAIDO\n"),
			if H#nd.pings_fallidos == 4 ->
				procesar_espera(T);
			   true ->
				[H#nd{pings_fallidos = H#nd.pings_fallidos + 1}] ++ procesar_espera(T)
			end;
		   true ->
			[H#nd{ping = 0}] ++ procesar_espera(T)
		end
end.

%-----------------------------------------------------------------------------
nuevo_nodo(NODO,F,P) -> #nd{nodo = NODO, pings_fallidos = F, ping = P}.
%%-----------------------------------------------------------------------------
