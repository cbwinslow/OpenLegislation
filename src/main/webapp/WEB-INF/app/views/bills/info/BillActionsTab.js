import React from "react"
import { CalendarBlank } from "phosphor-react";
import { formatDateTime } from "app/lib/dateUtils";
import { DateTime } from "luxon";
import { capitalize } from "app/lib/textUtils";
import SortBy, {
  ASC,
  DESC
} from "app/shared/SortBy";

export default function BillActionsTab({ bill }) {
  const [ actions, setActions ] = React.useState(bill.actions.items)
  const [ sort, setSort ] = React.useState(ASC)

  React.useEffect(() => {
    if (sort === ASC) {
      setActions(bill.actions.items)
    }
    if (sort === DESC) {
      setActions(bill.actions.items.slice().reverse())
    }
  }, [ sort ])

  return (
    <section className="m-5">
      <div className="mb-5">
        <label className="label label--top" htmlFor="sort">Sort by</label>
        <SortBy sort={sort} onChange={(value) => setSort(value)} name="sort" />
      </div>
      <div>
        {actions.map((action) => {
          return (
            <div className="mb-5 flex items-center gap-8" key={action.sequenceNo}>
              <div className="">
                <div className="w-40">
                  <CalendarBlank color="#374151" size="1rem" weight="regular" className="inline mr-1" />
                  <span>{formatDateTime(action.date, DateTime.DATE_FULL)}</span>
                </div>
                <div className="ml-5 text-gray-500 text text--small">
                  {capitalize(action.chamber)} <span className="text-gray-300">|</span> {action.billId.printNo}
                </div>
              </div>
              <div className="font-semibold">
                {capitalize(action.text)}
              </div>
            </div>
          )
        })}
      </div>
    </section>
  )
}
