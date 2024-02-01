import { h, Component } from "https://esm.sh/preact@10.19.2";
import htm from "https://esm.sh/htm@3.1.1";
import { effect } from "https://esm.sh/@preact/signals@1.2.1";
import { getAuth, getQuotes, setQuotes } from "./state.js";

const html = htm.bind(h);

const sortingOrder = ["First", "Business", "Economy"];

export class TrainSeats extends Component {
  constructor() {
    super();
    this.state = {
      train: null,
      seats: {},
      time: "",
    };
    effect(() => {
      this.setState({
        ...this.state,
        quotes: getQuotes(),
      });
    });
  }

  async componentDidMount() {
    const [, , trainCompany, trainId, time] = location.pathname.split("/");
    const response1 = await fetch(
      `/api/getTrain?trainCompany=${trainCompany}&trainId=${trainId}`,
      {
        headers: {
          Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
            false,
          )}`,
        },
      },
    );
    if (!response1.ok) {
      return html`${await response1.text()}`;
    }
    const train = await response1.json();

    const response2 = await fetch(
      `/api/getAvailableSeats?trainCompany=${trainCompany}&trainId=${trainId}&time=${time}`,
      {
        headers: {
          Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
            false,
          )}`,
        },
      },
    );
    if (!response2.ok) {
      return html`${await response2.text()}`;
    }
    const seats = await response2.json();

    this.setState({ train, seats, time });
  }

  render() {
    let quotes = this.state.quotes;
    const seatsInCart = new Set(quotes.map((quote) => quote.seatId));
    return html`
      <div class="page">
        <div class="trains-item">
          ${this.state.train != null
            ? html`
                <img src="${this.state.train.image}" />
                <div>
                  <div class="trains-item-name">${this.state.train.name}</div>
                  <div class="trains-item-location">
                    ${this.state.train.location}
                  </div>
                  <div class="train-time">${this.state.time}</div>
                </div>
              `
            : ""}
        </div>
        <div>
          ${Object.entries(this.state.seats)
            .sort(
              (a, b) => sortingOrder.indexOf(a[0]) - sortingOrder.indexOf(b[0]),
            )
            .map(
              ([name, seats]) => html`
                <div>
                  <div class="seats-type">${name}</div>
                  <div class="seats seats-${name}">
                    ${seats
                      .filter((seat) => !seatsInCart.has(seat.seatId))
                      .map(
                        (seat) => html`
                          <div
                            class="seat seat-${seat.name.slice(
                              seat.name.length - 1,
                            )}"
                          >
                            <button
                              class="seats-button"
                              onClick="${() => {
                                quotes = [
                                  ...quotes,
                                  {
                                    trainCompany: seat.trainCompany,
                                    trainId: seat.trainId,
                                    seatId: seat.seatId,
                                  },
                                ];
                                setQuotes(quotes);
                              }}"
                            >
                              ${seat.name}
                            </button>
                          </div>
                        `,
                      )}
                  </div>
                </div>
              `,
            )}
        </div>
      </div>
    `;
  }
}
