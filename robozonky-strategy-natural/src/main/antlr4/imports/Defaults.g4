grammar Defaults;

import Tokens;

@header {
    import com.github.robozonky.api.strategies.*;
    import com.github.robozonky.strategy.natural.*;
    import com.github.robozonky.strategy.natural.conditions.*;
}

defaultExpression returns [DefaultValues result]:
 r=portfolioExpression { $result = new DefaultValues($r.result); }
 (v=reservationExpression { $result.setReservationMode($v.result); })
 (i=defaultInvestmentSizeExpression { $result.setInvestmentSize($i.result); })?
 (b=defaultPurchaseSizeExpression { $result.setPurchaseSize($b.result); })?
 (p=targetPortfolioSizeExpression { $result.setTargetPortfolioSize($p.result); })?
 (e=exitDateExpression { $result.setExitProperties($e.result); })?
;

portfolioExpression returns [DefaultPortfolio result] :
    'Robot má udržovat ' (
        ( 'konzervativní' { $result = DefaultPortfolio.CONSERVATIVE; } )
        | ( 'balancované' { $result = DefaultPortfolio.BALANCED; } )
        | ( 'progresivní' { $result = DefaultPortfolio.PROGRESSIVE; } )
        | ( 'uživatelem definované' { $result = DefaultPortfolio.EMPTY; } )
    ) ' portfolio' DOT
;

exitDateExpression returns [ExitProperties result]:
    'Opustit Zonky k ' termination=dateExpr (
        ( { $result = new ExitProperties($termination.result); } )
        | (
            OR_COMMA 'výprodej zahájit ' selloff=dateExpr {
                $result = new ExitProperties($termination.result, $selloff.result);
            }
        )
    ) DOT
;

defaultInvestmentSizeExpression returns [int result] :
    'Robot má investovat do úvěrů po ' amount=intExpr KC '.' { $result = $amount.result; }
;

defaultPurchaseSizeExpression returns [int result] :
    'Robot má nakupovat participace nejvýše za ' amount=intExpr KC '.' { $result = $amount.result; }
;

defaultInvestmentShareExpression returns [DefaultInvestmentShare result] :
    'Investovat maximálně '
        maximumInvestmentInCzk=intExpr
        { $result = new DefaultInvestmentShare($maximumInvestmentInCzk.result); }
    ' % výše úvěru' DOT
;

reservationExpression returns [ReservationMode result] :
    (
        'Robot má pravidelně kontrolovat rezervační systém a přijímat rezervace půjček odpovídajících této strategii.' {
            $result = ReservationMode.ACCEPT_MATCHING;
        }
    ) | (
        'Robot má převzít kontrolu nad rezervačním systémem a přijímat rezervace půjček odpovídajících této strategii.' {
            $result = ReservationMode.FULL_OWNERSHIP;
        }
    )  | (
        'Robot má zcela ignorovat rezervační systém.' {
            $result = null;
        }
    )
;
