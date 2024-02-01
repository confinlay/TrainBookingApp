import { h, Component } from "https://esm.sh/preact@10.19.2";
import htm from "https://esm.sh/htm@3.1.1";
import { getAuth } from "./state.js";

const html = htm.bind(h);

export class TrainTimes extends Component {
  constructor() {
    super();
    this.state = {
      train: null,
      times: [],
    };
  }

  async componentDidMount() {
    const [, , trainCompany, trainId] = location.pathname.split("/");
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
      `/api/getTrainTimes?trainCompany=${trainCompany}&trainId=${trainId}`,
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
    const times = await response2.json();

    this.setState({ train, times });
  }

  render() {
    return html`
      <div class="page">
        <div class="trains-item">
          ${this.state.train != null
            ? html`
                <img async src="${this.state.train.image}" />
                <div>
                  <div class="trains-item-name">${this.state.train.name}</div>
                  <div class="trains-item-location">
                    ${this.state.train.location}
                  </div>
                </div>
              `
            : ""}
        </div>
        <div>
          ${this.state.times.map(
            (time) => html`
              <div class="train-time">
                <div>
                  ${Intl.DateTimeFormat("en-gb", {
                    dateStyle: "long",
                    timeStyle: "short",
                  }).format(new Date(time))}
                </div>
                <a
                  href="/trains/${this.state.train.trainCompany}/${this.state
                    .train.trainId}/${time}"
                >
                  <div class="train-times-button-book">Book now</div>
                </a>
              </div>
            `,
          )}
        </div>
      </div>
    `;
  }
}
