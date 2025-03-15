export default class PersonDto {
  id?: number
  name?: string
  istId?: string
  type?: string
  email?: string

  constructor(obj?: Partial<PersonDto>) {
    Object.assign(this, obj)
  }
}
