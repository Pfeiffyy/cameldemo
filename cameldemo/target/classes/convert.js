var _ = require('underscore');
var moment = require('moment');

var einheit = {
  "Packung": "Pkg.",
  "Stück": "Stk.",
  "Flaschen": "Fl."
};

var b = JSON.parse(request.getBody(Java.type("java.lang.String").class))

// Nur die Positionen, bei denen 'lieferbar' == true ist
var lieferbar = _.filter(b.positionen, function(p) {
  return p.lieferbar;
});

request.body = JSON.stringify({
  "id": b.nr,
  "datum": moment(b.datum, 'DD-MM-YYYY').format('YYYY-MM-DD'), 
  "kunde": b.kunde,
  "adresse": b.adresse,
  "pos": _.map(lieferbar, function(p, index) {//nur von denen die lieferbar sind
    return {
      "nr": index + 1,
      "menge": p.menge,
      "einheit": einheit[p.einheit],
      "artikel": p.beschreibung,
      "preis": p.preis,
    }
  }),
  //das ist der Gesamtpreis über alle Einzelpreise
  "gesamt-preis": _.reduce(lieferbar, function(memo, p) {
    return memo + p.preis * p.menge;
  }, 0)
})