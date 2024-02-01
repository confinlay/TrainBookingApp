import { h, Component } from "https://esm.sh/preact@10.19.2";
import { route } from "https://esm.sh/preact-router@4.1.2";
import htm from "https://esm.sh/htm@3.1.1";
import { effect } from "https://esm.sh/@preact/signals@1.2.1";
import { getAuth, getQuotes, setQuotes } from "./state.js";

const html = htm.bind(h);

export class Cart extends Component {
  constructor() {
    super();
    this.state = {
      trains: new Map(),
      seats: new Map(),
    };
    effect(() => {
      this.setState({
        ...this.state,
        quotes: getQuotes(),
      });
    });
  }

  async componentDidMount() {
    const quotes = this.state.quotes;
    const trains = new Map();
    const seats = new Map();
    for (const quote of quotes) {
      if (!trains.has(quote.trainId)) {
        const response = await fetch(
          `/api/getTrain?trainCompany=${quote.trainCompany}&trainId=${quote.trainId}`,
          {
            headers: {
              Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
                false,
              )}`,
            },
          },
        );
        if (!response.ok) {
          return html`${await response.text()}`;
        }
        const train = await response.json();
        trains.set(train.trainId, train);
      }
      if (!seats.has(quote.seatId)) {
        const response = await fetch(
          `/api/getSeat?trainCompany=${quote.trainCompany}&trainId=${quote.trainId}&seatId=${quote.seatId}`,
          {
            headers: {
              Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
                false,
              )}`,
            },
          },
        );
        if (!response.ok) {
          return html`${await response.text()}`;
        }
        const seat = await response.json();
        seats.set(seat.seatId, seat);
      }
    }

    this.setState({ trains, seats });
  }

  render() {
    return html`
      <div class="page">
        <div>
          <h1>Shopping cart</h1>
        </div>
        <div>
          ${this.state.quotes.map(
            (quote) => html`
              <div class="quote">
                <div>${this.state.trains.get(quote.trainId)?.name}</div>
                ${this.state.seats.has(quote.seatId)
                  ? html`
                      <div class="quote-time">
                        ${Intl.DateTimeFormat("en-gb", {
                          dateStyle: "long",
                          timeStyle: "short",
                        }).format(
                          new Date(this.state.seats.get(quote.seatId).time),
                        )}
                      </div>
                      <div class="quote-seat-type">
                        ${this.state.seats.get(quote.seatId).type}
                      </div>
                      <div class="quote-seat-name">
                        ${this.state.seats.get(quote.seatId).name}
                      </div>
                    `
                  : ""}
                <button
                  class="quote-remove-button"
                  onClick="${() => {
                    const quotes = this.state.quotes.filter(
                      (q) => q.seatId !== quote.seatId,
                    );
                    setQuotes(quotes);
                  }}"
                >
                  Remove
                </button>
              </div>
            `,
          )}
          ${this.state.quotes.length !== 0
            ? html`
                <button
                  class="quote-confirm-button"
                  onClick="${async () => {
                    const response = await fetch("/api/confirmQuotes", {
                      method: "POST",
                      body: JSON.stringify(this.state.quotes),
                      headers: {
                        Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
                          false,
                        )}`,
                        "Content-Type": "application/json",
                      },
                    });
                    if (response.ok) {
                      setQuotes([]);
                      route("/account");
                    }
                  }}"
                >
                  Book all
                </button>
              `
            : html` Your shopping cart is empty `}
        </div>
      </div>
    `;
  }
}
